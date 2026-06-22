package com.sean.payment_api.service

import com.sean.payment_api.data.AppUser
import com.sean.payment_api.data.AuthResponse
import com.sean.payment_api.data.ChangePasswordRequest
import com.sean.payment_api.data.ForgotPasswordRequest
import com.sean.payment_api.data.LoginRequest
import com.sean.payment_api.data.PasswordResetToken
import com.sean.payment_api.data.RegisterRequest
import com.sean.payment_api.data.ResetPasswordRequest
import com.sean.payment_api.data.UserProfileResponse
import com.sean.payment_api.exception.BadRequestException
import com.sean.payment_api.exception.ConflictException
import com.sean.payment_api.exception.NotFoundException
import com.sean.payment_api.exception.UnauthorizedException
import com.sean.payment_api.repository.PasswordResetTokenRepository
import com.sean.payment_api.repository.UserRepository
import com.sean.payment_api.security.JwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    @Value("\${app.frontend-url:http://localhost:5173}") private val frontendUrl: String,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val username = request.username.trim()
        val email = request.email.trim().lowercase()
        if (userRepository.existsByUsername(username)) {
            throw ConflictException("That username is already taken")
        }
        if (userRepository.existsByEmail(email)) {
            throw ConflictException("An account with this email already exists")
        }
        val user = AppUser(
            username = username,
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            email = email,
            birthday = request.birthday,
            passwordHash = passwordEncoder.encode(request.password)!!,
        )
        userRepository.save(user)
        return AuthResponse(token = jwtService.generate(user), username = user.username)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username.trim())
            ?: throw UnauthorizedException("Invalid username or password")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid username or password")
        }
        return AuthResponse(token = jwtService.generate(user), username = user.username)
    }

    @Transactional(readOnly = true)
    fun getProfile(userId: String): UserProfileResponse {
        val user = requireUser(userId)
        return UserProfileResponse(
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            birthday = user.birthday,
        )
    }

    @Transactional
    fun changePassword(userId: String, request: ChangePasswordRequest) {
        val user = requireUser(userId)
        if (!passwordEncoder.matches(request.currentPassword, user.passwordHash)) {
            throw BadRequestException("Current password is incorrect")
        }
        userRepository.save(user.copy(passwordHash = passwordEncoder.encode(request.newPassword)!!))
    }

    // Always succeeds (doesn't reveal whether the email exists); only sends a
    // reset link if there's a matching account.
    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest) {
        val user = userRepository.findByEmail(request.email.trim().lowercase()) ?: return
        resetTokenRepository.deleteByUserId(user.id)
        val token = resetTokenRepository.save(
            PasswordResetToken(
                userId = user.id,
                expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES),
            ),
        )
        emailService.sendPasswordReset(user.email, "$frontendUrl/reset-password?token=${token.token}")
    }

    @Transactional
    fun resetPassword(request: ResetPasswordRequest) {
        val token = resetTokenRepository.findById(request.token).orElse(null)
            ?: throw BadRequestException("Invalid or expired reset link")
        if (token.expiresAt.isBefore(Instant.now())) {
            resetTokenRepository.delete(token)
            throw BadRequestException("This reset link has expired")
        }
        val user = requireUser(token.userId)
        userRepository.save(user.copy(passwordHash = passwordEncoder.encode(request.newPassword)!!))
        resetTokenRepository.deleteByUserId(user.id)
    }

    /** Sends a test reminder email to the user; returns whether it was actually sent. */
    @Transactional(readOnly = true)
    fun sendTestReminder(userId: String): Boolean {
        val user = requireUser(userId)
        return emailService.sendTest(user.email)
    }

    private fun requireUser(userId: String): AppUser =
        userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
}
