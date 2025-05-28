package com.ticketera.auth.service


import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.AuthProvider
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
    private val authService = AuthService(userRepository, passwordEncoder, tokenManager)

    private val signInRequest = SignInRequest("user@email.com", "Joe Doe", "hashedpassword123")
    private val loginRequest = LoginRequest("user@email.com", "hashedpassword123")

    private val user = User(
        UUID.randomUUID(), "user@email.com", "Joe Doe", "encodedPassword",
        Role.USER.name, AuthProvider.LOCAL, false
    )

    @Test
    fun shouldNotRegisterTheUserIfTheEmailIsAlreadyInUse() {
        every { userRepository.existsByEmail(any()) } returns true

        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { authService.signIn(signInRequest) }
            .withMessage("Email already in use")

        verify { userRepository.existsByEmail(any()) }
    }

    @Test
    fun shouldRegisterAnewUser() {
        every { userRepository.existsByEmail(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { userRepository.save(any<User>()) } returns user

        authService.signIn(signInRequest)

        verify { userRepository.existsByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { passwordEncoder.encode(any()) }
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
    fun itShouldSaveAnewUser() {
        every { userRepository.findByEmail(any()) } returns null
        every { userRepository.save(any()) } returns user

        authService.findOrCreateUser("newuser@gmail.com", "Joe Doe")

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun itShouldUpdateAnExistingUser() {
        every { userRepository.findByEmail(any()) } returns user
        every { userRepository.save(any()) } returns user

        authService.findOrCreateUser(user.email, user.name)

        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun itVerifyUserRefreshTokeIsNull() {
        every { userRepository.findByEmail(any()) } returns user
        every { tokenManager.getUserEmailFromTokenString(any()) } returns user.email

        assertThat(authService.canRefresh(user.email)).isFalse()

        verify { tokenManager.getUserEmailFromTokenString(any()) }
        verify { userRepository.findByEmail(any()) }

    }
}