package com.ticketera.auth.dto.response

import java.util.UUID

data class LoginResponse(
    val accessToken: String,
    val refreshToken: UUID? = null,
    val tokenType: String = "Bearer"
)