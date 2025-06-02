package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
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
    fun logout(request: RefreshTokenRequest) {
        val user = userRepository.findByRefreshToken(request.refreshToken)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)
        userRepository.save(user.copy(refreshToken = null))
    }

    @Transactional
    fun refresh(request: RefreshTokenRequest): LoginResponse {
        val user = userRepository.findByRefreshToken(request.refreshToken)
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)
        val refreshToken = UUID.randomUUID()

        userRepository.save(user.copy(refreshToken = refreshToken))

        return LoginResponse(tokenManager.generateToken(user), refreshToken)
    }

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email) ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)
        val refreshToken = UUID.randomUUID()

        validateLogin(user, request)

        userRepository.save(user.copy(refreshToken = refreshToken))

        return LoginResponse(tokenManager.generateToken(user), refreshToken)
    }

    @Transactional
    fun signUp(signInRequest: SignUpRequest) {
        validateSignUp(signInRequest)
        val user = User(
            null,
            signInRequest.email,
            signInRequest.name,
            passwordEncoder.encode(signInRequest.pass),
            Role.USER.name,
            AuthProvider.LOCAL,
            false
        )

        userRepository.save(user).also {
            verifyUserService.sendVerificationEmail(user.email, VerifyEmailMessageKey.VERIFY_EMAIL)
        }
    }

    @Transactional
    fun findOrCreateUser(authData: OAuthData): User {
        return userRepository.findByEmail(authData.email)?.let {
            if (!it.isVerified) {
                verifyUserService.sendVerificationEmail(it.email, VerifyEmailMessageKey.NOT_VERIFIED_LOGIN)
                throw AuthException(Message.USER_NOT_VERIFIED.text)
            } else {
                userRepository.save(
                    it.copy(refreshToken = UUID.randomUUID())
                )
            }
        } ?: userRepository.save(
            User(
                email = authData.email,
                name = authData.name,
                password = "",
                roles = Role.USER.name,
                authProvider = AuthProvider.GOOGLE,
                isVerified = false,
                refreshToken = UUID.randomUUID()
            )
        ).also { verifyUserService.sendVerificationEmail(it.email, VerifyEmailMessageKey.VERIFY_EMAIL) }
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

    fun canRefresh(userData: String) =
        userRepository.findByEmail(tokenManager.getEncodedUserEmail(userData))?.refreshToken != null

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

    private fun validateSignUp(req: SignUpRequest) {
        userRepository.findByEmail(req.email)?.let {
            if (it.isVerified) throw IllegalArgumentException(Message.EMAIL_IN_USE.text)
            else {
                verifyUserService.sendVerificationEmail(req.email, VerifyEmailMessageKey.NOT_VERIFIED_SIGN_UP)
                throw AuthException(Message.USER_NOT_VERIFIED.text)
            }
        }
    }
}