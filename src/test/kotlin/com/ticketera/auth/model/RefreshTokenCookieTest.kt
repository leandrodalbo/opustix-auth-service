package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class RefreshTokenCookieTest {
    val uuid = UUID.randomUUID()

    @Test
    fun shouldGetAnHttpCookie() {
        val cookie = RefreshTokenCookie(uuid).cookie()

        assertThat(cookie.isHttpOnly).isTrue()
        assertThat(cookie.secure).isTrue()
        assertThat(cookie.path).isEqualTo("/")
        assertThat(cookie.maxAge).isEqualTo(7 * 24 * 60 * 60)
    }
}