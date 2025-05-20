package com.ticketera.auth.service

import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.model.ValidUser
import com.ticketera.auth.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): ValidUser {
        val user = userRepository.findByEmail(email)
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        return ValidUser(
            user.email,
            user.password,
            user.tokenString(),
            user.refreshToken,
            user.roles().map { GrantedAuthority { it.name } }
        )
    }


    fun findOrCreateUser(email: String): User {
        return userRepository.findByEmail(email).let {
            userRepository.save(
                it?.copy(refreshToken = UUID.randomUUID()) ?: User(
                    email = email,
                    password = "",
                    roles = Role.USER.name,
                    authProvider = AuthProvider.GOOGLE,
                    isVerified = false,
                    refreshToken = UUID.randomUUID()
                )
            )
        }
    }
}
