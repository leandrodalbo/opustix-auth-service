package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {

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
    fun itShouldGiveMeASetOfRoles() {
        assertThat(user.roles()).isEqualTo(setOf(Role.USER))
    }

    @Test
    fun itShouldGiveMeAUserWithMoreRoles() {
        assertThat(user.withRoles(setOf(Role.USER, Role.MANAGER)).roles).isEqualTo("USER,MANAGER")
    }

    @Test
    fun itShouldEncodeTheUser() {
        assertThat(user.encoded()).isNotNull()
    }

    @Test
    fun itShouldDecodeTheUser() {
        val base64Data = user.encoded()

        val decoded = User.decode(base64Data)

        assertThat(decoded.email).isEqualTo(user.email)
        assertThat(decoded.name).isEqualTo(user.name)
        assertThat(decoded.roles).isEqualTo(user.roles)
        assertThat(decoded.authProvider).isEqualTo(user.authProvider)
        assertThat(decoded.isVerified).isEqualTo(user.isVerified)
    }

}