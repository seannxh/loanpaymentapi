package com.sean.payment_api.service

import com.sean.payment_api.data.CreateLoanRequest
import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.InstallmentStatus
import com.sean.payment_api.data.Loan
import com.sean.payment_api.data.LoanDetailResponse
import com.sean.payment_api.data.LoanRequest
import com.sean.payment_api.data.LoanScheduleRepayment
import com.sean.payment_api.data.LoanSummaryResponse
import com.sean.payment_api.data.UpdateLoanRequest
import com.sean.payment_api.exception.BadRequestException
import com.sean.payment_api.exception.NotFoundException
import com.sean.payment_api.repository.InstallmentRepository
import com.sean.payment_api.repository.LoanRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class LoanService(
    private val loanRepository: LoanRepository,
    private val installmentRepository: InstallmentRepository,
) {

    // ---- Calculation (preview, not persisted) ----

    fun calculateSchedule(request: LoanRequest): LoanScheduleRepayment {
        val installments = buildInstallments("preview", request, LocalDate.now())
        return LoanScheduleRepayment(
            purchaseAmount = request.purchaseAmount,
            apr = request.annualPercentageRate,
            totalInterest = installments.sumOf { it.interest },
            totalAmount = installments.sumOf { it.paymentAmount },
            installments = installments,
        )
    }

    // ---- Persistent, user-scoped loans ----

    @Transactional
    fun createLoan(userId: String, request: CreateLoanRequest): LoanDetailResponse {
        val startDate = request.startDate ?: LocalDate.now()
        val loan = loanRepository.save(
            Loan(
                userId = userId,
                name = request.name.trim(),
                category = request.category.trim().ifBlank { "Other" },
                purchaseAmount = request.purchaseAmount,
                apr = request.annualPercentageRate,
                numberOfInstallments = request.numberOfInstallments,
                startDate = startDate,
            ),
        )
        val installments = buildInstallments(
            loanId = loan.id,
            request = LoanRequest(
                purchaseAmount = request.purchaseAmount,
                annualPercentageRate = request.annualPercentageRate,
                numberOfInstallments = request.numberOfInstallments,
            ),
            startDate = startDate,
        )
        installmentRepository.saveAll(installments)
        return toDetail(loan, installments)
    }

    @Transactional(readOnly = true)
    fun getUserLoans(userId: String): List<LoanSummaryResponse> =
        loanRepository.findByUserId(userId)
            .sortedByDescending { it.createdAt }
            .map { loan -> toSummary(loan, installmentRepository.findByLoanId(loan.id)) }

    @Transactional(readOnly = true)
    fun getLoan(userId: String, loanId: String): LoanDetailResponse {
        val loan = requireOwnedLoan(userId, loanId)
        val installments = installmentRepository.findByLoanId(loanId)
            .sortedBy { it.installmentNumber }
        return toDetail(loan, installments)
    }

    @Transactional
    fun updateStatus(userId: String, installmentId: String, status: InstallmentStatus): Installment {
        val installment = installmentRepository.findById(installmentId)
            .orElseThrow { NotFoundException("Installment not found") }
        // Ownership check: the installment's loan must belong to this user.
        requireOwnedLoan(userId, installment.loanId)

        when (installment.status) {
            InstallmentStatus.PAID -> throw BadRequestException("This installment is already paid")
            InstallmentStatus.FAILED -> throw BadRequestException("Cannot update a failed installment")
            InstallmentStatus.PENDING, InstallmentStatus.OVERDUE -> {}
        }

        // Payments must be made in order: every earlier installment must be paid first.
        if (status == InstallmentStatus.PAID) {
            val earliestUnpaid = installmentRepository.findByLoanId(installment.loanId)
                .filter { it.installmentNumber < installment.installmentNumber && it.status != InstallmentStatus.PAID }
                .minByOrNull { it.installmentNumber }
            if (earliestUnpaid != null) {
                throw BadRequestException("Please pay installment #${earliestUnpaid.installmentNumber} first")
            }
        }
        return installmentRepository.save(installment.copy(status = status))
    }

    @Transactional
    fun updateLoan(userId: String, loanId: String, request: UpdateLoanRequest): LoanDetailResponse {
        val loan = requireOwnedLoan(userId, loanId)
        val updated = loan.copy(
            name = request.name?.trim()?.ifBlank { loan.name } ?: loan.name,
            category = request.category?.trim()?.ifBlank { loan.category } ?: loan.category,
        )
        loanRepository.save(updated)
        val installments = installmentRepository.findByLoanId(loanId).sortedBy { it.installmentNumber }
        return toDetail(updated, installments)
    }

    @Transactional
    fun deleteLoan(userId: String, loanId: String) {
        val loan = requireOwnedLoan(userId, loanId)
        installmentRepository.deleteByLoanId(loanId)
        loanRepository.delete(loan)
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun checkOverdueInstallments() {
        val overdue = installmentRepository.findByStatusAndDueDateBefore(
            InstallmentStatus.PENDING,
            LocalDate.now(),
        )
        installmentRepository.saveAll(overdue.map { it.copy(status = InstallmentStatus.OVERDUE) })
    }

    // ---- Helpers ----

    private fun requireOwnedLoan(userId: String, loanId: String): Loan =
        loanRepository.findByIdAndUserId(loanId, userId)
            .orElseThrow { NotFoundException("Loan not found") }

    /**
     * Builds a properly amortized repayment schedule. The monthly payment is sized
     * with the standard annuity formula M = P·r·(1+r)^n / ((1+r)^n − 1) so that the
     * sum of all payments equals principal + interest and the balance reaches 0.
     */
    private fun buildInstallments(
        loanId: String,
        request: LoanRequest,
        startDate: LocalDate,
    ): List<Installment> {
        val n = request.numberOfInstallments
        val monthlyRate = request.annualPercentageRate
            .divide(BigDecimal(100), 12, RoundingMode.HALF_UP)
            .divide(BigDecimal(12), 12, RoundingMode.HALF_UP)
        val onePlusRPowN = BigDecimal.ONE.add(monthlyRate).pow(n)
        val monthlyPayment = request.purchaseAmount
            .multiply(monthlyRate)
            .multiply(onePlusRPowN)
            .divide(onePlusRPowN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP)

        var balance = request.purchaseAmount
        val installments = mutableListOf<Installment>()
        for (i in 1..n) {
            val interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP)
            var principal = monthlyPayment.subtract(interest)
            var payment = monthlyPayment
            if (i == n) {
                // Final payment clears any rounding remainder so the balance hits 0.
                principal = balance
                payment = principal.add(interest)
            }
            balance = balance.subtract(principal)
            installments.add(
                Installment(
                    loanId = loanId,
                    installmentNumber = i,
                    dueDate = startDate.plusMonths(i.toLong()),
                    paymentAmount = payment,
                    principal = principal,
                    interest = interest,
                    remainingBalance = balance,
                    status = InstallmentStatus.PENDING,
                ),
            )
        }
        return installments
    }

    private class Totals(
        val totalAmount: BigDecimal,
        val totalInterest: BigDecimal,
        val totalPaid: BigDecimal,
        val paidCount: Int,
    )

    private fun totalsOf(installments: List<Installment>): Totals {
        val totalAmount = installments.sumOf { it.paymentAmount }
        val totalPaid = installments
            .filter { it.status == InstallmentStatus.PAID }
            .sumOf { it.paymentAmount }
        return Totals(
            totalAmount = totalAmount,
            totalInterest = installments.sumOf { it.interest },
            totalPaid = totalPaid,
            paidCount = installments.count { it.status == InstallmentStatus.PAID },
        )
    }

    private fun toSummary(loan: Loan, installments: List<Installment>): LoanSummaryResponse {
        val t = totalsOf(installments)
        val nextDue = installments
            .filter { it.status != InstallmentStatus.PAID }
            .minByOrNull { it.installmentNumber }
        return LoanSummaryResponse(
            id = loan.id,
            name = loan.name,
            category = loan.category,
            purchaseAmount = loan.purchaseAmount,
            apr = loan.apr,
            numberOfInstallments = loan.numberOfInstallments,
            startDate = loan.startDate,
            totalAmount = t.totalAmount,
            totalPaid = t.totalPaid,
            totalRemaining = t.totalAmount.subtract(t.totalPaid),
            paidCount = t.paidCount,
            totalCount = installments.size,
            nextDueDate = nextDue?.dueDate,
            nextPaymentAmount = nextDue?.paymentAmount,
        )
    }

    private fun toDetail(loan: Loan, installments: List<Installment>): LoanDetailResponse {
        val t = totalsOf(installments)
        return LoanDetailResponse(
            id = loan.id,
            name = loan.name,
            category = loan.category,
            purchaseAmount = loan.purchaseAmount,
            apr = loan.apr,
            numberOfInstallments = loan.numberOfInstallments,
            startDate = loan.startDate,
            totalInterest = t.totalInterest,
            totalAmount = t.totalAmount,
            totalPaid = t.totalPaid,
            totalRemaining = t.totalAmount.subtract(t.totalPaid),
            paidCount = t.paidCount,
            installments = installments,
        )
    }
}
