package com.sean.payment_api.repository

import com.sean.payment_api.data.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<AppUser, String> {
    fun findByUsername(username: String): AppUser?
    fun findByEmail(email: String): AppUser?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
