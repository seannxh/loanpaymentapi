package com.sean.payment_api.repository

import com.sean.payment_api.data.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, String> {
    fun deleteByUserId(userId: String)
}
