package com.sean.payment_api.repository

import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.InstallmentStatus
import org.apache.logging.log4j.util.Strings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface LoanRepository: JpaRepository<Installment, String> {
    fun findByLoanId(loanId: String): List<Installment>
    fun findByLoanIdAndStatus(loanId: String, status: InstallmentStatus): List<Installment>
    fun findByPaymentAmount(paymentAmount: BigDecimal): List<Installment>
    fun findByLoanIdAndPaymentAmount(loanId: String, paymentAmount: BigDecimal): List<Installment>
}