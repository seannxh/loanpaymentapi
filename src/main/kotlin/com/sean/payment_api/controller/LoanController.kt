package com.sean.payment_api.controller

import com.sean.payment_api.data.Installment
import com.sean.payment_api.data.LoanRequest
import com.sean.payment_api.data.LoanScheduleRepayment
import com.sean.payment_api.data.UpdateStatusRequest
import com.sean.payment_api.service.LoanService
import jakarta.persistence.Id
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/loans")
class LoanController(private val loanService: LoanService) {

    @PostMapping("/calculate")
    fun calculateLoan(@RequestBody request: LoanRequest): LoanScheduleRepayment{
        return loanService.calculateSchedule(request)
    }
    @GetMapping("/{loanId}/installments")
    fun getUserInstallments(@PathVariable loanId: String): List<Installment>{
        return loanService.getInstallments(loanId)
    }

    @PatchMapping("/installments/{installmentId}")
    fun updateInstallment(@PathVariable installmentId: String, @RequestBody updateStatusRequest: UpdateStatusRequest): Installment{
        return loanService.updateStatus(installmentId, updateStatusRequest.status)
    }

}