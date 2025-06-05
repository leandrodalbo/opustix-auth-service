package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.errors.Message
import com.ticketera.auth.dto.request.SignUpRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.User
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.OAuthData
import com.ticketera.auth.model.AuthPair
import com.ticketera.auth.model.RefreshTokenCookie
import com.ticketera.auth.model.VerifyEmailMessageKey
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenManager: TokenManager,
    private val verifyUserService: VerifyUserService
) {

    @Transactional
    fun logout(refreshToken: String) {
        val token = UUID.fromString(refreshToken)
        val user = userRepository.findByRefreshToken(token)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)
        userRepository.save(user.withoutRefreshToken(token))
    }

    @Transactional
    fun refresh(refreshToken: String): AuthPair {
        val token = UUID.fromString(refreshToken)
        val user = userRepository.findByRefreshToken(token)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)

        if (user.refreshTokens.any { it.token == token && it.isExpired() }) {
            userRepository.save(
                user.withoutRefreshToken(token)
            )

            throw AuthException(Message.INVALID_TOKEN.text)
        }

        val newToken = UUID.randomUUID()

        userRepository.save(
            user.withoutRefreshToken(token)
                .withNewRefreshToken(newToken)
        )

        return AuthPair(
            LoginResponse(tokenManager.generateToken(user)),
            RefreshTokenCookie(newToken).cookie()
        )
    }

    @Transactional
    fun login(request: LoginRequest): AuthPair {
        val user = userRepository.findByEmail(request.email) ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)
        val refreshToken = UUID.randomUUID()

        validateLogin(user, request)

        userRepository.save(user.withNewRefreshToken(refreshToken))

        return AuthPair(
            LoginResponse(tokenManager.generateToken(user)),
            RefreshTokenCookie(refreshToken).cookie()
        )
    }

    @Transactional
    fun signUp(signInRequest: SignUpRequest) {
        val user = validatedSignUp(signInRequest.email, AuthProvider.LOCAL)
            ?.withAddedAuthProvider(AuthProvider.LOCAL)
            ?.copy(name = signInRequest.name, password = passwordEncoder.encode(signInRequest.pass), isVerified = false)
            ?: User(
                null,
                signInRequest.email,
                signInRequest.name,
                passwordEncoder.encode(signInRequest.pass),
                Role.USER.name,
                AuthProvider.LOCAL.name,
                false
            )

        userRepository.save(user)
        verifyUserService.sendVerificationEmail(user.email, VerifyEmailMessageKey.VERIFY_EMAIL)
    }

    @Transactional
    fun oauthSignUp(authData: OAuthData, refreshToken: UUID, authProvider: AuthProvider): User {
        val user = validatedSignUp(authData.email, authProvider)
            .let { found ->
                found?.withNewRefreshToken(refreshToken)
                    .also {
                        if (it?.authProviders()?.contains(authProvider) == false)
                            it.withAddedAuthProvider(authProvider)
                    }
                    ?: User(
                        email = authData.email,
                        name = authData.name,
                        password = "",
                        roles = Role.USER.name,
                        authProviders = authProvider.name,
                        isVerified = false
                    ).withNewRefreshToken(refreshToken)
            }

        val savedUser = userRepository.save(user)
        verifyUserService.sendVerificationEmail(user.email, VerifyEmailMessageKey.VERIFY_EMAIL)
        return savedUser
    }

    @Transactional
    fun verifyUser(token: String) {
        val toVerify = verifyUserService.findFromToken(token)
        userRepository.findByEmail(toVerify.email)
            ?.let {
                userRepository.save(it.copy(isVerified = true))
            } ?: throw AuthException(Message.REQUEST_FAILED.text)

        verifyUserService.sendVerificationEmail(toVerify.email, VerifyEmailMessageKey.SUCCESSFULLY_VERIFIED)
    }

    private fun validateLogin(user: User, req: LoginRequest) {
        if (!passwordEncoder.matches(
                req.pass,
                user.password
            )
        ) throw InvalidUserException(Message.INVALID_PASSWORD.text)

        if (!user.isVerified) {
            verifyUserService.sendVerificationEmail(req.email, VerifyEmailMessageKey.NOT_VERIFIED_LOGIN)
            throw AuthException(Message.USER_NOT_VERIFIED.text)
        }
    }

    private fun validatedSignUp(email: String, authProvider: AuthProvider): User? {
        userRepository.findByEmail(email).let {
            if (authProvider == AuthProvider.LOCAL && it?.authProviders()
                    ?.contains(authProvider) == true && it.isVerified
            ) throw IllegalArgumentException(Message.EMAIL_IN_USE.text)

            if (it?.authProviders()?.contains(authProvider) == true && !it.isVerified
            ) {
                verifyUserService.sendVerificationEmail(email, VerifyEmailMessageKey.NOT_VERIFIED_SIGN_UP)
                throw AuthException(Message.USER_NOT_VERIFIED.text)
            }
            return it
        }
    }
}