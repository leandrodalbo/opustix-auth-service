package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
import com.ticketera.auth.errors.Message
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenManager: TokenManager
) {

    fun logout(request: RefreshTokenRequest) {
        val user = userRepository.findByRefreshToken(request.refreshToken)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)
        userRepository.save(user.copy(refreshToken = null))
    }

    fun refresh(request: RefreshTokenRequest): LoginResponse {
        val user = userRepository.findByRefreshToken(request.refreshToken)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)

        val refreshToken = UUID.randomUUID()

        userRepository.save(user.copy(refreshToken = refreshToken))

        return LoginResponse(tokenManager.generateToken(user), refreshToken)
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        if (!passwordEncoder.matches(
                request.pass,
                user.password
            )
        ) throw InvalidUserException(Message.INVALID_PASSWORD.text)

        val refreshToken = UUID.randomUUID()

        userRepository.save(user.copy(refreshToken = refreshToken))

        return LoginResponse(tokenManager.generateToken(user), refreshToken)
    }

    fun signIn(signInRequest: SignInRequest) {

        if (userRepository.existsByEmail(signInRequest.email)) {
            throw IllegalArgumentException(Message.EMAIL_IN_USE.text)
        }

        val user = User(
            null,
            signInRequest.email,
            passwordEncoder.encode(signInRequest.pass),
            Role.USER.name,
            AuthProvider.LOCAL,
            false
        )

        userRepository.save(user)

    }


    fun findOrCreateUser(email: String): User {
        return userRepository.findByEmail(email).let {
            userRepository.save(
                it?.copy(refreshToken = UUID.randomUUID()) ?: User(
                    email = email,
                    password = "",
                    roles = Role.USER.name,
                    authProvider = AuthProvider.GOOGLE,
                    isVerified = false,
                    refreshToken = UUID.randomUUID()
                )
            )
        }
    }

    fun canRefresh(tokenString: String) =
        userRepository.findByEmail(tokenManager.getUserEmailFromTokenString(tokenString))?.refreshToken != null

}