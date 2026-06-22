package com.sean.payment_api.repository

import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.InstallmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface InstallmentRepository : JpaRepository<Installment, String> {
    fun findByLoanId(loanId: String): List<Installment>
    fun findByLoanIdAndStatus(loanId: String, status: InstallmentStatus): List<Installment>
    fun findByStatusAndDueDateBefore(status: InstallmentStatus, date: LocalDate): List<Installment>
    fun findByStatusAndReminderSentFalseAndDueDateBetween(
        status: InstallmentStatus,
        start: LocalDate,
        end: LocalDate,
    ): List<Installment>
    fun deleteByLoanId(loanId: String)
}
