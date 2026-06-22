package com.sean.payment_api.controller

import com.sean.payment_api.data.AuthResponse
import com.sean.payment_api.data.AuthUser
import com.sean.payment_api.data.ChangePasswordRequest
import com.sean.payment_api.data.ForgotPasswordRequest
import com.sean.payment_api.data.LoginRequest
import com.sean.payment_api.data.RegisterRequest
import com.sean.payment_api.data.ResetPasswordRequest
import com.sean.payment_api.data.UserProfileResponse
import com.sean.payment_api.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse =
        authService.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        authService.login(request)

    // Current user's profile (requires a valid token).
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: AuthUser): UserProfileResponse =
        authService.getProfile(user.id)

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal user: AuthUser,
        @Valid @RequestBody request: ChangePasswordRequest,
    ) = authService.changePassword(user.id, request)

    // Public: emails a reset link if the address matches an account.
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest) =
        authService.forgotPassword(request)

    // Public: completes a reset using the emailed token.
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest) =
        authService.resetPassword(request)

    // Sends a test reminder email to the current user. Returns { sent: true|false }.
    @PostMapping("/test-reminder")
    fun testReminder(@AuthenticationPrincipal user: AuthUser): Map<String, Boolean> =
        mapOf("sent" to authService.sendTestReminder(user.id))
}
