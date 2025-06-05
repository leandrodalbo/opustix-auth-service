package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class RefreshTokenTest {
    val user = User(
        null,
        "user@mail.com",
        "Joe Doe",
        "a32dga34adfv34",
        Role.USER.name,
        AuthProvider.LOCAL,
        false

    )


    @Test
    fun itCheckTheTokenIsExpired() {
        val refreshToken = RefreshToken(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
            user
        )

        assertThat(refreshToken.isExpired()).isTrue()
    }

    @Test
    fun shouldOverrideToString() {
        val uuid = UUID.randomUUID()
        val refreshToken = RefreshToken(
            uuid,
            uuid,
            Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
            user
        )
        assertThat(refreshToken.toString()).isEqualTo("id:${uuid}|user:${user.email}|token:${uuid}")
    }

    @Test
    fun shouldHashCode() {
        val uuid = UUID.randomUUID()
        val refreshToken = RefreshToken(
            uuid,
            uuid,
            Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
            user
        )
        assertThat(refreshToken.hashCode()).isEqualTo(uuid.hashCode())
    }

    @Test
    fun shouldOverrideToEquals() {
        val uuid = UUID.randomUUID()
        val refreshToken = RefreshToken(
            uuid,
            uuid,
            Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
            user
        )
        assertThat(refreshToken == refreshToken.copy()).isTrue()
    }
}