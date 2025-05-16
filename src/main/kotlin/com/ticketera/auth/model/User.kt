package com.ticketera.auth.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "roles", nullable = false)
    val roles: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    val authProvider: AuthProvider,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean
) {

    fun roles(): Set<Role> = roles.split(",").map { Role.valueOf(it) }.toSet()

    fun withRoles(roles: Set<Role>) =
        copy(this.id, this.email, this.password, roles.joinToString(","), this.authProvider, this.isVerified)


    fun tokenString() = "email:${this.email}|roles:${this.roles}|authprovider:${authProvider}|verified:${isVerified}"
}
