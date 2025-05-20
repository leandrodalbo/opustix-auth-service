package com.ticketera.auth.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.util.UUID

class OAuth2Handler(
    private val userService: UserService,
    private val tokenManager: TokenManager,

) : AuthenticationSuccessHandler {

    private val objectMapper = ObjectMapper()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal = authentication.principal as OAuth2User
        val email = principal.getAttribute<String>("email") ?: return

        val user = userService.findOrCreateUser(email)

        val accessToken = tokenManager.generateToken(user)

        val loginResponse = LoginResponse(accessToken, user.refreshToken)

        response.contentType = "application/json"
        response.writer.write(objectMapper.writeValueAsString(loginResponse))
    }
}