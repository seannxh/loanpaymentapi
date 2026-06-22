package com.sean.payment_api.repository

import com.sean.payment_api.data.Loan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface LoanRepository : JpaRepository<Loan, String> {
    fun findByUserId(userId: String): List<Loan>
    fun findByIdAndUserId(id: String, userId: String): Optional<Loan>
}
