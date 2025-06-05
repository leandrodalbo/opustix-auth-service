package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class VerifyUserTest {
    val uuid = UUID.randomUUID()

    val verifyUser = VerifyUser(
        uuid,
        "user@mail.com",
        Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli(),
    )

    @Test
    fun itCheckTheTokenIsExpired() {
        assertThat(verifyUser.isExpired()).isTrue()
    }

    @Test
    fun shouldOverrideToString() {
        assertThat(verifyUser.toString()).isEqualTo("token:${verifyUser.token}|email:${verifyUser.email}|expiry:${verifyUser.expiry}")
    }

    @Test
    fun shouldHashCode() {
        assertThat(verifyUser.hashCode()).isEqualTo(uuid.hashCode())
    }

    @Test
    fun shouldOverrideToEquals() {
        assertThat(verifyUser == verifyUser.copy()).isTrue()
    }
}