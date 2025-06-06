package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.NewPasswordRequest
import com.ticketera.auth.dto.request.NewPasswordTokenRequest
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
import com.ticketera.auth.model.EmailMessageKey
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenManager: TokenManager,
    private val notificationsService: UserNotificationsService
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

        localLoginValidation(user, request)

        userRepository.save(user.withNewRefreshToken(refreshToken))

        return AuthPair(
            LoginResponse(tokenManager.generateToken(user)),
            RefreshTokenCookie(refreshToken).cookie()
        )
    }

    @Transactional
    fun signUp(signUpRequest: SignUpRequest) =
        userRepository.findByEmail(signUpRequest.email)
            ?.let {
                signUpAddingLocalAuth(signUpRequest, it)
            } ?: newLocalSignUp(signUpRequest)

    @Transactional
    fun handleOauth(authData: OAuthData, refreshToken: UUID, authProvider: AuthProvider) =
        userRepository.findByEmail(authData.email)?.let {
            oAuthLogin(it, authProvider, refreshToken)
        } ?: oAuthSignup(authData, authProvider, refreshToken)


    @Transactional
    fun verifyUser(token: String) {
        val toVerify = notificationsService.findFromToken(token)
        userRepository.findByEmail(toVerify.email)
            ?.let {
                userRepository.save(it.copy(isVerified = true))
                notificationsService.sendVerificationEmail(toVerify.email, EmailMessageKey.SUCCESSFULLY_VERIFIED)
            } ?: throw AuthException(Message.REQUEST_FAILED.text)
    }

    @Transactional
    fun setPasswordToken(newPasswordRequest: NewPasswordTokenRequest) {
        val user = userRepository.findByEmail(newPasswordRequest.email)
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        val saved = userRepository.save(
            user.copy(
                passwordResetToken = UUID.randomUUID(),
                passwordResetTokenExpiry = Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
            )
        )
        notificationsService.sendPasswordResetNotifications(
            user.email,
            EmailMessageKey.PASSWORD_RESET,
            saved.passwordResetToken
        )
    }

    @Transactional
    fun setNewPassword(newPasswordRequest: NewPasswordRequest) {
        val user = userRepository.findByPasswordResetToken(UUID.fromString(newPasswordRequest.token))
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)

        val saved = userRepository.save(
            user.copy(
                password = passwordEncoder.encode(newPasswordRequest.pass),
                passwordResetToken = null,
                passwordResetTokenExpiry = null
            )
        )

        notificationsService.sendPasswordResetNotifications(
            user.email,
            EmailMessageKey.PASSWORD_CHANGED,
            saved.passwordResetToken
        )
    }

    private fun localLoginValidation(user: User, req: LoginRequest) {
        if (!passwordEncoder.matches(
                req.pass,
                user.password
            )
        ) throw InvalidUserException(Message.INVALID_PASSWORD.text)

        if (!user.isVerified) {
            notificationsService.sendVerificationEmail(req.email, EmailMessageKey.NOT_VERIFIED_LOGIN)
            throw AuthException(Message.USER_NOT_VERIFIED.text)
        }
    }

    private fun oAuthLogin(user: User, authProvider: AuthProvider, refreshToken: UUID): User {
        if (!user.isVerified) {
            notificationsService.sendVerificationEmail(user.email, EmailMessageKey.NOT_VERIFIED_LOGIN)
            throw AuthException(Message.USER_NOT_VERIFIED.text)
        }

        return if (!user.authProviders().contains(authProvider))
            userRepository.save(
                user.withAddedAuthProvider(authProvider)
                    .withNewRefreshToken(refreshToken)
            )
        else
            userRepository.save(user.withNewRefreshToken(refreshToken))
    }

    private fun oAuthSignup(authData: OAuthData, authProvider: AuthProvider, refreshToken: UUID): User {
        val user = User(
            email = authData.email,
            name = authData.name,
            password = "",
            roles = Role.USER.name,
            authProviders = authProvider.name,
            isVerified = false
        ).withNewRefreshToken(refreshToken)

        val savedUser = userRepository.save(user)
        notificationsService.sendVerificationEmail(user.email, EmailMessageKey.VERIFY_EMAIL)
        return savedUser
    }

    private fun newLocalSignUp(signUpRequest: SignUpRequest) {
        userRepository.save(
            User(
                null,
                signUpRequest.email,
                signUpRequest.name,
                passwordEncoder.encode(signUpRequest.pass),
                Role.USER.name,
                AuthProvider.LOCAL.name,
                false
            )
        )
        notificationsService.sendVerificationEmail(signUpRequest.email, EmailMessageKey.VERIFY_EMAIL)
    }

    private fun signUpAddingLocalAuth(signUpRequest: SignUpRequest, user: User) {
        if (!user.isVerified) {
            notificationsService.sendVerificationEmail(user.email, EmailMessageKey.NOT_VERIFIED_SIGN_UP)
            throw AuthException(Message.USER_NOT_VERIFIED.text)
        }

        if (!user.authProviders().contains(AuthProvider.LOCAL)) {
            userRepository.save(
                user
                    .copy(
                        name = signUpRequest.name,
                        password = passwordEncoder.encode(signUpRequest.pass),
                        isVerified = false
                    )
                    .withAddedAuthProvider(AuthProvider.LOCAL)
            )
            notificationsService.sendVerificationEmail(user.email, EmailMessageKey.VERIFY_EMAIL)
        } else
            throw AuthException(Message.EMAIL_IN_USE.text)
    }
}