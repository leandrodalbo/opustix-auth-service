package com.ticketera.auth.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import java.util.UUID
import java.util.Base64

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = false)
    val name: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "roles", nullable = false)
    val roles: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    val authProvider: AuthProvider,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean,

    @Column(name = "refresh_token", nullable = true)
    val refreshToken: UUID? = null
) {

    fun roles(): Set<Role> = roles.split(",").map { Role.valueOf(it) }.toSet()

    fun withRoles(roles: Set<Role>) =
        copy(this.id, this.email, this.name, this.password, roles.joinToString(","), this.authProvider, this.isVerified)


    fun tokenString() =
        Base64.getEncoder()
            .encodeToString("${this.email}${TOKEN_SEPARATOR}${this.name}${TOKEN_SEPARATOR}${this.roles}${TOKEN_SEPARATOR}${authProvider}${TOKEN_SEPARATOR}${isVerified}${TOKEN_SEPARATOR}${refreshToken ?: DEFAULT_REFRESH_VALUE}".toByteArray())

    companion object {
        val TOKEN_SEPARATOR = "|"
        val DEFAULT_REFRESH_VALUE = "NO_TOKEN"
    }
}
