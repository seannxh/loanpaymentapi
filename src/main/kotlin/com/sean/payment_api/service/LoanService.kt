package com.sean.payment_api.service

import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.InstallmentStatus
import com.sean.payment_api.data.LoanRequest
import com.sean.payment_api.data.LoanScheduleRepayment
import com.sean.payment_api.repository.LoanRepository
import jakarta.persistence.Id
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.FlexibleTypeDeserializer.ThrowException

@Service
class LoanService(private val loanRepository: LoanRepository){

    //class ResourceNotFoundException(message: String) : RuntimeException(message)

    fun calculateSchedule(request: LoanRequest) : LoanScheduleRepayment {

        val loanId: String = UUID.randomUUID().toString()
        val monthlyApr: BigDecimal = request.annualPercentageRate.divide(BigDecimal(12), 10, RoundingMode.HALF_UP)
            .divide(BigDecimal(100), 10, RoundingMode.UP)

        val monthlyPayment = request.purchaseAmount.divide((BigDecimal(request.numberOfInstallments)), 2, RoundingMode.HALF_UP)
        var remainingBalance = request.purchaseAmount
        val installments = mutableListOf<Installment>()
        for (i in 1..request.numberOfInstallments) {
            val interestPayment: BigDecimal = remainingBalance.multiply(monthlyApr)
            val principalPayment: BigDecimal = monthlyPayment.subtract(interestPayment)
            remainingBalance = remainingBalance.subtract(principalPayment)
            installments.add(
                Installment(
                    loanId = loanId,
                    installmentNumber = i,
                    dueDate = LocalDate.now().plusMonths(i.toLong()),
                    paymentAmount = monthlyPayment,
                    principal = principalPayment,
                    interest = interestPayment,
                    remainingBalance = remainingBalance,
                    status = InstallmentStatus.PENDING
                )
            )

        }
        return LoanScheduleRepayment(
            purchaseAmount = request.purchaseAmount,
            apr = request.annualPercentageRate,
            totalInterest = installments.sumOf { it.interest },
            totalAmount = installments.sumOf { it.paymentAmount },
            installments = installments
        )
    }

    fun getInstallments(loanId: String): List<Installment> {
        return loanRepository.findByLoanId(loanId)
    }

    fun updateStatus(installmentId: String, status: InstallmentStatus): Installment{
        val installment = loanRepository.findById(installmentId)
            .orElseThrow{IllegalStateException("Installment Not Found")}
        when(installment.status) {
            InstallmentStatus.PAID -> throw IllegalStateException("Installments are all paid")
            InstallmentStatus.PENDING -> {}
            InstallmentStatus.OVERDUE -> {}
            InstallmentStatus.FAILED ->  throw IllegalStateException("Cannot update a failed installment")
        }
        val updated = installment.copy(status = status)
        return loanRepository.save(updated)
    }

}