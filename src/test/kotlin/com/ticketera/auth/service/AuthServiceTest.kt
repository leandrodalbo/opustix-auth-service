package com.ticketera.auth.service


import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthServiceTest {

    private val passwordEncoder: PasswordEncoder = mockk()
    private val userRepository: UserRepository = mockk()

    private val authService = AuthService(userRepository, passwordEncoder)

    private val signInRequest = SignInRequest("user@email.com", "1aads@34b")

    private val user = User(
        UUID.randomUUID(), "user@email.com", "encodedPassword",
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

        assertThat(authService.signIn(signInRequest)).isEqualTo(user.id)

        verify { userRepository.existsByEmail(any()) }
        verify { userRepository.save(any()) }
        verify { passwordEncoder.encode(any()) }
    }

}