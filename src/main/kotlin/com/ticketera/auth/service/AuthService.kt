package com.ticketera.auth.service

import com.ticketera.auth.errors.Message
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {


    fun signIn(signInRequest: SignInRequest): UUID? {

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

        return userRepository.save(user).id

    }

}