package com.sean.payment_api.data

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
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
    @field:NotNull(message = "purchase amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val purchaseAmount: BigDecimal,

    @field:NotNull(message = "APR is required")
    @field:DecimalMin(value = "0.01", message = "APR has to be greater than .01")
    val annualPercentageRate: BigDecimal,

    @field:Min(value = 1, message = "Must have at least 1 installments")
    @field:Max(value = 60, message = "Cannot exceed 60 installments")
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