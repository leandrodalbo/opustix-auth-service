package com.ticketera.auth

import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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

}