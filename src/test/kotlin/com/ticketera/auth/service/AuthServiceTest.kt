package com.ticketera.auth.service

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.SignUpRequest
import com.ticketera.auth.errors.AuthException
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.User
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.OAuthData
import com.ticketera.auth.model.VerifyUser
import com.ticketera.auth.repository.UserRepository
import io.mockk.mockk
import io.mockk.verify
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
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
    fun registrationFailIfEmailIsAlreadyInUse() {
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
    fun wontLoginWhenThePasswordIsNotMatching() {
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
        every { userRepository.save(any()) } returns user.withNewRefreshToken(refreshToken = UUID.randomUUID())

        assertThat(authService.login(loginRequest)).isNotNull()

        verify { userRepository.findByEmail(any()) }
        verify { passwordEncoder.matches(any(), any()) }
        verify { tokenManager.generateToken(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun shouldRefreshAUserToken() {
        val refreshToken = UUID.randomUUID()
        every { userRepository.findByRefreshToken(any()) } returns user.withNewRefreshToken(refreshToken)
        every { userRepository.save(any()) } returns user.withoutRefreshToken(refreshToken)
            .withNewRefreshToken(UUID.randomUUID())
        every { tokenManager.generateToken(any()) } returns "9some4user2token0"

        assertThat(authService.refresh(refreshToken.toString()).cookie.value).isNotEqualTo(refreshToken.toString())

        verify { userRepository.findByRefreshToken(any()) }
        verify { userRepository.save(any()) }
        verify { tokenManager.generateToken(any()) }
    }

    @Test
    fun shouldNotRefreshAUserToken() {
        every { userRepository.findByRefreshToken(any()) } returns null

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy { authService.refresh(UUID.randomUUID().toString()) }
            .withMessage(Message.INVALID_TOKEN.text)

        verify { userRepository.findByRefreshToken(any()) }
    }

    @Test
    fun shouldFailOnLogout() {
        every { userRepository.findByRefreshToken(any()) } returns null

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy { authService.logout(UUID.randomUUID().toString()) }
            .withMessage(Message.INVALID_TOKEN.text)

        verify { userRepository.findByRefreshToken(any()) }
    }

    @Test
    fun shouldSuccessfullyLogout() {
        val refreshToken = UUID.randomUUID()
        every { userRepository.findByRefreshToken(any()) } returns user.copy(refreshToken)
        every { userRepository.save(any()) } returns user.withoutRefreshToken(refreshToken = refreshToken)

        authService.logout(refreshToken.toString())

        verify { userRepository.findByRefreshToken(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun aNewUserIsSavedVerificationEmailIsSent() {
        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } returns user
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        authService.findOrCreateUser(OAuthData("newuser@gmail.com", "Joe Doe"), UUID.randomUUID())

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun newRefreshTokenForAnExistingUser() {
        every { userRepository.findByEmail(any()) } returns user
        every { userRepository.save(any()) } returns user
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        authService.findOrCreateUser(OAuthData(user.email, user.name), UUID.randomUUID())

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun refreshTokenIsNotSavedForNotVerifiedUsers() {
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.findOrCreateUser(OAuthData(user.email, user.name), UUID.randomUUID()) }
            .withMessage(Message.USER_NOT_VERIFIED.text)


        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }


    @Test
    fun loginFailForNotVerifiedUsers() {
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
    fun signUpFailedForExistingUserThatIsNotVerified() {
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.signUp(signInRequest) }
            .withMessage(Message.USER_NOT_VERIFIED.text)

        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun shouldVerifyAnewUser() {
        every { verifyUserService.findFromToken(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "someuser@mail.com",
            Instant.now().toEpochMilli()
        )
        every { userRepository.findByEmail(any()) } returns user.copy(isVerified = false)
        every { userRepository.save(any()) } returns user.copy(isVerified = true)
        every { verifyUserService.sendVerificationEmail(any(), any()) } returns Unit

        authService.verifyUser(user.id.toString())

        verify { userRepository.save(any()) }
        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.findFromToken(any()) }
        verify { verifyUserService.sendVerificationEmail(any(), any()) }
    }

    @Test
    fun shouldNotVerifyAUserNotFound() {
        every { verifyUserService.findFromToken(any()) } returns VerifyUser(
            UUID.randomUUID(),
            "someuser@mail.com",
            Instant.now().toEpochMilli()
        )
        every { userRepository.findByEmail(any()) } returns null

        assertThatExceptionOfType(AuthException::class.java)
            .isThrownBy { authService.verifyUser(user.id.toString()) }

        verify { userRepository.findByEmail(any()) }
        verify { verifyUserService.findFromToken(any()) }
    }
}