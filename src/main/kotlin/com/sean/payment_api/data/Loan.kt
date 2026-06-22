package com.sean.payment_api.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "loans", indexes = [Index(name = "idx_loans_user", columnList = "userId")])
data class Loan(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val name: String,

    // DB default keeps `ddl-auto=update` happy when adding this column to a
    // loans table that already has rows.
    @Column(nullable = false, columnDefinition = "varchar(255) default 'Other'")
    val category: String = "Other",

    val purchaseAmount: BigDecimal,
    val apr: BigDecimal,
    val numberOfInstallments: Int,

    // First installment is due one month after startDate. Backdating this is how
    // a user records a "standing" loan they already have partway through.
    val startDate: LocalDate,

    val createdAt: Instant = Instant.now(),
)
