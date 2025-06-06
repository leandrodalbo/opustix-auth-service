package com.ticketera.auth.conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.OAuthData
import com.ticketera.auth.model.RefreshTokenCookie
import com.ticketera.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OAuth2Handler(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
) : AuthenticationSuccessHandler {

    private val objectMapper = ObjectMapper()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuthData = this.extractAuthData(authentication.principal as OAuth2User)
        val refreshToken = UUID.randomUUID()
        val user = authService.handleOauth(oAuthData, refreshToken, AuthProvider.GOOGLE)
        val loginResponse = LoginResponse(tokenManager.generateToken(user))

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.addCookie(RefreshTokenCookie(refreshToken).cookie())
        response.writer.write(objectMapper.writeValueAsString(loginResponse))
    }

    private fun extractAuthData(user: OAuth2User): OAuthData {
        val email = user.getAttribute<String>("email") ?: throw AccessDeniedException(Message.REQUEST_FAILED.text)
        val name = user.getAttribute<String>("name") ?: throw AccessDeniedException(Message.REQUEST_FAILED.text)

        return OAuthData(email, name)
    }
}