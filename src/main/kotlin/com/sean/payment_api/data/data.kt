package com.sean.payment_api.data

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


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
    @Id
    val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val installmentNumber: Int,
    val dueDate: LocalDate,
    val paymentAmount: BigDecimal,
    val principal: BigDecimal,
    val interest: BigDecimal,
    val remainingBalance: BigDecimal,
    @Enumerated(EnumType.STRING)
    val status: InstallmentStatus = InstallmentStatus.PENDING

)

data class LoanScheduleRepayment(
    val purchaseAmount: BigDecimal,
    val apr: BigDecimal,
    val totalInterest: BigDecimal,
    val totalAmount: BigDecimal,
    val installments: List<Installment>
)