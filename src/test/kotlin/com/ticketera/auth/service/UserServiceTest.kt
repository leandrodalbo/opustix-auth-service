package com.ticketera.auth.service

import com.ticketera.auth.conf.TokenAuth
import com.ticketera.auth.dto.request.UserRoleChange
import com.ticketera.auth.dto.request.UserRoleRequest
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

import java.util.UUID


class UserServiceTest {

    private val userRepository: UserRepository = mockk()
    private val tokenAuth: TokenAuth = mockk()
    private val userService = UserService(tokenAuth, userRepository)

    private val adminUser = User(
        UUID.randomUUID(), "user@email.com", "Joe Doe", "encodedPassword",
        Role.ADMIN.name, AuthProvider.LOCAL.name, true
    )

    @Test
    fun shouldSaveUserWithAnewRole() {
        every { userRepository.findByEmail(any()) } returns adminUser
        every { userRepository.save(any()) } returns adminUser
        every { tokenAuth.getAuthenticatedUserEmail() } returns adminUser.email

        userService.setUserRole(UserRoleRequest("user@email.com", Role.MANAGER, UserRoleChange.ADD))

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun shouldSaveUserWithoutTheRole() {
        every { userRepository.findByEmail(any()) } returns adminUser
        every { userRepository.save(any()) } returns adminUser
        every { tokenAuth.getAuthenticatedUserEmail() } returns adminUser.email

        userService.setUserRole(UserRoleRequest("user@email.com", Role.MANAGER, UserRoleChange.REMOVE))

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }
        verify { userRepository.save(any()) }
    }

    @Test
    fun shouldNotSaveUserIfNotLoggedIn() {
        every { userRepository.findByEmail(any()) } returns null
        every { tokenAuth.getAuthenticatedUserEmail() } returns ""

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy {
                userService.setUserRole(UserRoleRequest("user@email.com", Role.MANAGER, UserRoleChange.ADD))
            }

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }

    }

    @Test
    fun shouldNotSaveUserIfIsNotAdmin() {
        every { userRepository.findByEmail(any()) } returns adminUser.withoutRole(Role.ADMIN)
        every { tokenAuth.getAuthenticatedUserEmail() } returns "admin@mail.com"

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy {
                userService.setUserRole(UserRoleRequest("user@email.com", Role.MANAGER, UserRoleChange.ADD))
            }

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }

    }

    @Test
    fun shouldNotDeleteTheUser() {
        every { userRepository.findByEmail(any()) } returns null
        every { tokenAuth.getAuthenticatedUserEmail() } returns "admin@mail.com"

        assertThatExceptionOfType(InvalidUserException::class.java)
            .isThrownBy {
                userService.deleteUser()
            }

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }

    }

    @Test
    fun shouldDeleteTheUser() {
        every { userRepository.findByEmail(any()) } returns adminUser
        every { userRepository.delete(adminUser) } returns Unit
        every { tokenAuth.getAuthenticatedUserEmail() } returns "admin@mail.com"

        userService.deleteUser()

        verify { tokenAuth.getAuthenticatedUserEmail() }
        verify { userRepository.findByEmail(any()) }
        verify { userRepository.delete(any()) }
    }
}