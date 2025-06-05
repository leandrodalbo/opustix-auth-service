package com.ticketera.auth.service

import com.ticketera.auth.conf.TokenAuth
import com.ticketera.auth.dto.request.UpdateUserDetails
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.model.Role
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder

class UserService(
    private val tokenAuth: TokenAuth,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun updateUserDetails(userDetails: UpdateUserDetails) {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        val toBeSaved = user.copy(
            name = userDetails.name ?: user.name,
            password = userDetails.password?.let {
                passwordEncoder.encode(it)
            } ?: user.password
        )

        userRepository.save(toBeSaved)
    }

    fun addUserRole(role: Role) {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        userRepository.save(user.withAddedRole(role))
    }

    fun removeUserRole(role: Role) {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        userRepository.save(user.withoutRole(role))
    }

    fun deleteUser() {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        userRepository.delete(user)
    }
}