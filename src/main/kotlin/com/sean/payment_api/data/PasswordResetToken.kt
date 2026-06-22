package com.sean.payment_api.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
data class PasswordResetToken(
    @Id
    val token: String = UUID.randomUUID().toString(),
    val userId: String,
    val expiresAt: Instant,
)
