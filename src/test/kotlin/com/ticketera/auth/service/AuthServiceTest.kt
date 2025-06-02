package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
import com.ticketera.auth.dto.request.SignUpRequest
import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.OAuthData
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import io.mockk.mockk
import io.mockk.verify
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceTest {

    private val passwordEncoder: PasswordEncoder = mockk()
    private val userRepository: UserRepository = mockk()
    private val tokenManager: TokenManager = mockk()
    private val verifyUserService: VerifyUserService = mockk()
    private val authService = AuthService(userRepository, passwordEncoder, tokenManager, verifyUserService)

    private val signInRequest = SignUpRequest("user@email.com", "Joe Doe", "hashedpassword123")
    private val loginRequest = LoginRequest("user@email.com", "hashedpassword123")

    private val user = User(
        UUID.randomUUID(), "user@email.com", "Joe Doe", "encodedPassword",
        Role.USER.name, AuthProvider.LOCAL, true
    )

    @Test
    fun shouldNotRegisterTheUserIfTheEmailIsAlreadyInUse() {
        every { userRepository.findByEmail(any()) } returns user

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { authService.signUp(signInRequest) }
            .withMessage("Email already in use")

        verify { userRepository.findByEmail(any()) }
    }

    @Test
    fun shouldRegisterAnewUser() {
        every { userRepository.findByEmail(any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { userRepository.save(any<User>()) } returns user
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        authService.signUp(signInRequest)

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { passwordEncoder.encode(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }


    @Test
    fun shouldNotLoginThePasswordIsNotMatching() {
        every { userRepository.findByEmail(any()) } returns user.copy(password = "invalidpass")
        every { passwordEncoder.matches(any(), any()) } returns false

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy { authService.login(loginRequest) }
            .withMessage(Message.INVALID_PASSWORD.text)

        verify { userRepository.findByEmail(any()) }
        verify { passwordEncoder.matches(any(), any()) }
    }

    @Test
    fun shouldSuccessfullyLogin() {
        every { userRepository.findByEmail(any()) } returns user
        every { passwordEncoder.matches(any(), any()) } returns true
        every { tokenManager.generateToken(any()) } returns "9some4user2token0"
        every { userRepository.save(any()) } returns user.copy(refreshToken = UUID.randomUUID())

        assertThat(authService.login(loginRequest)).isNotNull()

        verify { userRepository.findByEmail(any()) }
        verify { passwordEncoder.matches(any(), any()) }
        verify { tokenManager.generateToken(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun shouldRefreshAUserToken() {
        val refreshToken = UUID.randomUUID()
        every { userRepository.findByRefreshToken(any()) } returns user.copy(refreshToken)
        every { userRepository.save(any()) } returns user.copy(refreshToken = UUID.randomUUID())
        every { tokenManager.generateToken(any()) } returns "9some4user2token0"

        assertThat(authService.refresh(RefreshTokenRequest(refreshToken)).refreshToken).isNotEqualTo(refreshToken)

        verify { userRepository.findByRefreshToken(any()) }
        verify { userRepository.save(any()) }
        verify { tokenManager.generateToken(any()) }
    }

    @Test
    fun shouldNotRefreshAUserToken() {
        every { userRepository.findByRefreshToken(any()) } returns null

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy { authService.refresh(RefreshTokenRequest(UUID.randomUUID())) }
            .withMessage(Message.INVALID_TOKEN.text)

        verify { userRepository.findByRefreshToken(any()) }
    }

    @Test
    fun shouldFailOnLogout() {
        every { userRepository.findByRefreshToken(any()) } returns null

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy { authService.logout(RefreshTokenRequest(UUID.randomUUID())) }
            .withMessage(Message.INVALID_TOKEN.text)

        verify { userRepository.findByRefreshToken(any()) }
    }

    @Test
    fun shouldSuccessfullyLogout() {
        val refreshToken = UUID.randomUUID()
        every { userRepository.findByRefreshToken(any()) } returns user.copy(refreshToken)
        every { userRepository.save(any()) } returns user.copy(refreshToken = null)

        authService.logout(RefreshTokenRequest(refreshToken))

        verify { userRepository.findByRefreshToken(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun itShouldSaveAnewUserAndSendAVerificationEmail() {
        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } returns user
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        authService.findOrCreateUser(OAuthData("newuser@gmail.com", "Joe Doe"))

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun itShouldRefreshAnExistingUser() {
        every { userRepository.findByEmail(any()) } returns user
        every { userRepository.save(any()) } returns user

        authService.findOrCreateUser(OAuthData(user.email, user.name))

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun itShouldNotUpdateANotVerifiedUser() {
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.findOrCreateUser(OAuthData(user.email, user.name)) }
            .withMessage(Message.USER_NOT_VERIFIED.text)


        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun itVerifyUserRefreshTokeIsNull() {
        every { userRepository.findByEmail(any()) } returns user
        every { tokenManager.getEncodedUserEmail(any()) } returns user.email

        assertThat(authService.canRefresh(user.email)).isFalse()

        verify { tokenManager.getEncodedUserEmail(any()) }
        verify { userRepository.findByEmail(any()) }
    }

    @Test
    fun shouldThrowNotVerifiedUserOnLogin() {
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { passwordEncoder.matches(any(), any()) } returns true
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.login(loginRequest) }
            .withMessage(Message.USER_NOT_VERIFIED.text)

        verify { userRepository.findByEmail(any()) }
        verify { passwordEncoder.matches(any(), any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun shouldThrowNotVerifiedUserOnSignUp() {
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.signUp(signInRequest) }
            .withMessage(Message.USER_NOT_VERIFIED.text)

        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }
}