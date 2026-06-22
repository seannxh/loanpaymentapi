package com.sean.payment_api.data

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

// At least 6 characters, with an uppercase letter, a number, and a special character.
const val PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$"
const val PASSWORD_MESSAGE =
    "Password must include an uppercase letter, a number, and a special character"

data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Pattern(
        regexp = "^[A-Za-z0-9_]{3,20}$",
        message = "Username must be 3-20 letters, numbers, or underscores",
    )
    val username: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Email(message = "A valid email is required")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotNull(message = "Birthday is required")
    @field:Past(message = "Birthday must be in the past")
    val birthday: LocalDate,

    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    val password: String,
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    val newPassword: String,
)

data class ForgotPasswordRequest(
    @field:Email(message = "A valid email is required")
    @field:NotBlank(message = "Email is required")
    val email: String,
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    val token: String,

    @field:Size(min = 6, message = "Password must be at least 6 characters")
    @field:Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_MESSAGE)
    val newPassword: String,
)

data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String,
)

data class AuthResponse(
    val token: String,
    val username: String,
)

data class UserProfileResponse(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val birthday: LocalDate,
)

// The authenticated principal placed in the SecurityContext by JwtAuthFilter,
// available in controllers via @AuthenticationPrincipal.
data class AuthUser(
    val id: String,
    val username: String,
)
