package com.sean.payment_api.data

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jdk.jfr.DataAmount
import java.math.BigDecimal
import java.time.LocalDate




enum class  InstallmentStatus {
    PENDING,
    PAID,
    OVERDUE,
    FAILED
}
data class LoanRequest(
    val purchaseAmount: BigDecimal,
    val annualPercentageRate: BigDecimal,
    val numberOfInstallments: Int
)

@Entity
@Table(name = "installments")
data class Installment(
    val installmentNumber: Int,
    val dueData: LocalDate,
    val paymentAmount: BigDecimal,
    val principal: BigDecimal,
    val interest: BigDecimal,
    val remainingBalance: BigDecimal,
    val status: InstallmentStatus = InstallmentStatus.PENDING
)

data class LoanScheduleRepayment(
    val purchaseAmount: BigDecimal,
    val apr: BigDecimal,
    val totalInterest: BigDecimal,
    val totalAmount: DataAmount,
    val installments: List<Installment>
)