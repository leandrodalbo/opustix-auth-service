package com.ticketera.auth.dto.response

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)