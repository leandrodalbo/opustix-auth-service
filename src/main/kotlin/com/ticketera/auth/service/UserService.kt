package com.ticketera.auth.service

import com.ticketera.auth.conf.TokenAuth
import com.ticketera.auth.dto.request.UserRoleChange
import com.ticketera.auth.dto.request.UserRoleRequest
import com.ticketera.auth.errors.InvalidRoleUpdate
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val tokenAuth: TokenAuth,
    private val userRepository: UserRepository
) {

    fun setUserRole(userRoleRequest: UserRoleRequest) {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)
        if (!user.roles().contains(Role.ADMIN)) {
            throw InvalidUserException(Message.NOT_ADMIN_USER.text)
        } else {
            userRepository.save(roleChange(userRoleRequest) ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text))
        }
    }

    fun deleteUser() {
        val user = userRepository.findByEmail(tokenAuth.getAuthenticatedUserEmail())
            ?: throw InvalidUserException(Message.EMAIL_NOT_FOUND.text)

        userRepository.delete(user)
    }

    private fun roleChange(userRoleRequest: UserRoleRequest): User? = when (userRoleRequest.userRoleChange) {
        UserRoleChange.ADD -> userRepository.findByEmail(userRoleRequest.email)
            ?.let {
                if (!it.roles().contains(userRoleRequest.role))
                    it.withAddedRole(userRoleRequest.role)
                else
                    throw InvalidRoleUpdate(Message.USER_ALREADY_CONTAINS_THE_ROLE.text)
            }

        UserRoleChange.REMOVE -> userRepository.findByEmail(userRoleRequest.email)
            ?.let {
                if (it.roles().contains(userRoleRequest.role))
                    it.withoutRole(userRoleRequest.role)
                else
                    throw InvalidRoleUpdate(Message.USER_DOES_NOT_CONTAIN_THE_ROLE.text)
            }
    }
}