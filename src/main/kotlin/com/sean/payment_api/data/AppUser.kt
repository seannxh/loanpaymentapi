package com.sean.payment_api.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

// Named AppUser (table app_users) because "USER" is a reserved word in H2.
@Entity
@Table(name = "app_users")
data class AppUser(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val birthday: LocalDate,

    @Column(nullable = false)
    val passwordHash: String,

    val createdAt: Instant = Instant.now(),
)
