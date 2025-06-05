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
import jakarta.persistence.OneToMany
import jakarta.persistence.CascadeType
import java.time.Instant
import java.time.temporal.ChronoUnit
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

    @Column(name = "auth_providers", nullable = false)
    val authProviders: String,

    @Column(name = "is_verified", nullable = false)
    val isVerified: Boolean,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val refreshTokens: Set<RefreshToken> = emptySet()
) {

    fun roles(): Set<Role> = roles.split(",").map { Role.valueOf(it) }.toSet()

    fun withAddedRole(role: Role): User {
        val updatedRoles = this.roles() + role
        return this.copy(roles = updatedRoles.joinToString(","))
    }

    fun withoutRole(role: Role): User {
        val updatedRoles = this.roles().filter { it != role }
        return this.copy(roles = updatedRoles.joinToString(","))
    }

    fun authProviders(): Set<AuthProvider> = authProviders.split(",").map { AuthProvider.valueOf(it) }.toSet()

    fun withAddedAuthProvider(authProvider: AuthProvider): User {
        val updatedProviders = this.authProviders() + authProvider
        return this.copy(authProviders = updatedProviders.joinToString(","))
    }

    fun withNewRefreshToken(refreshToken: UUID): User {
        val tokens = refreshTokens + RefreshToken(
            null,
            refreshToken, Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli(), this
        )
        return this.copy(refreshTokens = tokens)
    }

    fun withoutRefreshToken(refreshToken: UUID): User {
        val tokens = refreshTokens.filter { it.token != refreshToken }.toSet()
        return this.copy(refreshTokens = tokens)
    }

    override fun toString(): String {
        return "id:${id}|email:${email}"
    }

    override fun equals(other: Any?) = this === other || (other is User && id == other.id)
    override fun hashCode() = id.hashCode()

    fun encoded(): String {
        val data = EncodedUser(
            email,
            name,
            roles,
            authProviders,
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
            @JsonProperty("authProviders") val authProviders: String,
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
                userData.authProviders,
                userData.isVerified
            )
        }

        private val mapper = ObjectMapper()
    }
}
