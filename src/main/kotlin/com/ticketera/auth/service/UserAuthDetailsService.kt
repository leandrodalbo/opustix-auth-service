package com.ticketera.auth.service

import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message

import com.ticketera.auth.repository.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserAuthDetailsService(private val userRepository: UserRepository) :
    UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        return org.springframework.security.core.userdetails.User(
            user.email,
            user.password,
            user.roles().map { GrantedAuthority { it.name } }
        )
    }
}
