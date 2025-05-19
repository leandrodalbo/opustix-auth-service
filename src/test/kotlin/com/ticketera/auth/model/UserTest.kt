package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {

    val user = User(
        null,
        "user@mail.com",
        "a32dga34adfv34",
        Role.USER.name,
        AuthProvider.LOCAL,
        false

    )

    @Test
    fun itShouldGiveMeASetOfRoles() {
        assertThat(user.roles()).isEqualTo(setOf(Role.USER))
    }

    @Test
    fun itShouldGiveMeAUserWithMoreRoles() {
        assertThat(user.withRoles(setOf(Role.USER, Role.MANAGER)).roles).isEqualTo("USER,MANAGER")
    }

    @Test
    fun itShouldGenerateATokenString() {
        val token = UUID.randomUUID();
        assertThat(user.copy(refreshToken = token).tokenString()).isEqualTo("user@mail.com|USER|LOCAL|false|${token}")
    }

}