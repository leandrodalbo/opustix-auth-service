package com.ticketera.auth.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import java.time.Instant
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

    fun encoded(): String {
        val data = EncodedUser(
            email,
            name,
            roles,
            authProvider.name,
            isVerified,
            Instant.now().toEpochMilli()
        )

        return Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(data))
    }


    companion object {

        private data class EncodedUser
        @JsonCreator constructor(
            @JsonProperty("email") val email: String,
            @JsonProperty("name") val name: String,
            @JsonProperty("roles") val roles: String,
            @JsonProperty("authProvider") val authProvider: String,
            @JsonProperty("verified") val isVerified: Boolean,
            @JsonProperty("timestamp") val timestamp: Long
        )

        fun decode(data: String): User {
            val decoded = Base64.getDecoder().decode(data)
            val userData = mapper.readValue(decoded, EncodedUser::class.java)
            return User(
                null,
                userData.email,
                userData.name,
                "",
                userData.roles,
                AuthProvider.valueOf(userData.authProvider),
                userData.isVerified,
                null
            )
        }

        private val mapper = ObjectMapper()
    }

}
