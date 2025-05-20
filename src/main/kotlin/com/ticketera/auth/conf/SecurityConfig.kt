package com.ticketera.auth.conf

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenAuth: OncePerRequestFilter,
    private val oAuth2Handler: OAuth2Handler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/auth/**","/oauth2/**","/login/oauth2/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .oauth2Login {
                it.successHandler(oAuth2Handler)
            }
            .addFilterBefore(tokenAuth, UsernamePasswordAuthenticationFilter::class.java)

        http.exceptionHandling {
            it.authenticationEntryPoint { _, response, _ ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            }
            it.accessDeniedHandler { _, response, _ ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN)
            }
        }

        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
}