package com.ticketera.auth.model

import jakarta.servlet.http.Cookie
import java.util.UUID

data class RefreshTokenCookie(val token: UUID?) {
    fun cookie() =
        Cookie("refreshToken", token.toString()).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 7 * 24 * 60 * 60
        }
}
