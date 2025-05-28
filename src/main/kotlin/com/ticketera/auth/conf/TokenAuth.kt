package com.ticketera.auth.conf

import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class TokenAuth(
    private val tokenManager: TokenManager,
    private val userDetailsService: UserDetailsService,
    private val authService: AuthService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)

        if (token != null && SecurityContextHolder.getContext().authentication == null) {

            val user = userDetailsService.loadUserByUsername(tokenManager.getUserEmailFromToken(token))

            if (!authService.canRefresh(user.username)) throw AccessDeniedException(Message.INVALID_TOKEN.text)

            if (tokenManager.isAValidToken(token)) {
                val authToken =
                    UsernamePasswordAuthenticationToken(user, user.password, user.authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}
