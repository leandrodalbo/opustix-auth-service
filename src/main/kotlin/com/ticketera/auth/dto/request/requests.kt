package com.ticketera.auth.dto.request


import com.ticketera.auth.model.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Full name is required")
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:Pattern(
        regexp = "^(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$",
        message = "Password must contain at least one special character"
    )
    val pass: String
)

data class LoginRequest(val email: String, val pass: String)

data class UserRoleRequest(val email: String, val role: Role, val userRoleChange: UserRoleChange)

enum class UserRoleChange {
    ADD, REMOVE
}