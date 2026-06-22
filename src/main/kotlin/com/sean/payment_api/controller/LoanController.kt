package com.sean.payment_api.controller

import com.sean.payment_api.data.AuthUser
import com.sean.payment_api.data.CreateLoanRequest
import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.LoanDetailResponse
import com.sean.payment_api.data.LoanRequest
import com.sean.payment_api.data.LoanScheduleRepayment
import com.sean.payment_api.data.LoanSummaryResponse
import com.sean.payment_api.data.UpdateLoanRequest
import com.sean.payment_api.data.UpdateStatusRequest
import com.sean.payment_api.service.LoanService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans")
class LoanController(private val loanService: LoanService) {

    // Preview a repayment schedule without saving it.
    @PostMapping("/calculate")
    fun calculateLoan(@Valid @RequestBody request: LoanRequest): LoanScheduleRepayment =
        loanService.calculateSchedule(request)

    // List the current user's saved loans (dashboard).
    @GetMapping
    fun myLoans(@AuthenticationPrincipal user: AuthUser): List<LoanSummaryResponse> =
        loanService.getUserLoans(user.id)

    // Save a calculated loan to the current user's account.
    @PostMapping
    fun createLoan(
        @AuthenticationPrincipal user: AuthUser,
        @Valid @RequestBody request: CreateLoanRequest,
    ): LoanDetailResponse = loanService.createLoan(user.id, request)

    // One loan plus its installment schedule (payment tracker).
    @GetMapping("/{loanId}")
    fun getLoan(
        @AuthenticationPrincipal user: AuthUser,
        @PathVariable loanId: String,
    ): LoanDetailResponse = loanService.getLoan(user.id, loanId)

    // Rename a loan or change its category.
    @PatchMapping("/{loanId}")
    fun updateLoan(
        @AuthenticationPrincipal user: AuthUser,
        @PathVariable loanId: String,
        @RequestBody request: UpdateLoanRequest,
    ): LoanDetailResponse = loanService.updateLoan(user.id, loanId, request)

    @PatchMapping("/installments/{installmentId}")
    fun updateInstallment(
        @AuthenticationPrincipal user: AuthUser,
        @PathVariable installmentId: String,
        @RequestBody updateStatusRequest: UpdateStatusRequest,
    ): Installment = loanService.updateStatus(user.id, installmentId, updateStatusRequest.status)

    // Delete a loan and all of its installments.
    @DeleteMapping("/{loanId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLoan(
        @AuthenticationPrincipal user: AuthUser,
        @PathVariable loanId: String,
    ) = loanService.deleteLoan(user.id, loanId)
}
