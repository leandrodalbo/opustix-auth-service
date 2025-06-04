package com.ticketera.auth.model

import com.ticketera.auth.dto.response.LoginResponse
import jakarta.servlet.http.Cookie

data class AuthPair(val loginResponse: LoginResponse, val cookie: Cookie)
