package com.sean.payment_api.data

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

// Request to save a loan to the user's account.
data class CreateLoanRequest(
    @field:NotBlank(message = "A loan name is required")
    val name: String,

    val category: String = "Other",

    @field:NotNull(message = "purchase amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val purchaseAmount: BigDecimal,

    @field:NotNull(message = "APR is required")
    @field:DecimalMin(value = "0.01", message = "APR has to be greater than .01")
    val annualPercentageRate: BigDecimal,

    @field:Min(value = 1, message = "Must have at least 1 installments")
    @field:Max(value = 60, message = "Cannot exceed 60 installments")
    val numberOfInstallments: Int,

    // Optional. Defaults to today. Backdate for a loan already in progress.
    val startDate: LocalDate? = null,
)

// Edit a loan's label/category (financial terms are not editable).
data class UpdateLoanRequest(
    val name: String? = null,
    val category: String? = null,
)

// Compact view of a loan for the dashboard list.
data class LoanSummaryResponse(
    val id: String,
    val name: String,
    val category: String,
    val purchaseAmount: BigDecimal,
    val apr: BigDecimal,
    val numberOfInstallments: Int,
    val startDate: LocalDate,
    val totalAmount: BigDecimal,
    val totalPaid: BigDecimal,
    val totalRemaining: BigDecimal,
    val paidCount: Int,
    val totalCount: Int,
    // Next unpaid installment, or null when the loan is fully paid.
    val nextDueDate: LocalDate?,
    val nextPaymentAmount: BigDecimal?,
)

// Full view of a loan including its installment schedule.
data class LoanDetailResponse(
    val id: String,
    val name: String,
    val category: String,
    val purchaseAmount: BigDecimal,
    val apr: BigDecimal,
    val numberOfInstallments: Int,
    val startDate: LocalDate,
    val totalInterest: BigDecimal,
    val totalAmount: BigDecimal,
    val totalPaid: BigDecimal,
    val totalRemaining: BigDecimal,
    val paidCount: Int,
    val installments: List<Installment>,
)
