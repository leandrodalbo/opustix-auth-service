package com.ticketera.auth.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {

    val user = User(
        null,
        "user@mail.com",
        "Joe Doe",
        "a32dga34adfv34",
        Role.USER.name,
        AuthProvider.LOCAL.name,
        false
    )

    @Test
    fun itHasASetOfRoles() {
        assertThat(user.roles()).isEqualTo(setOf(Role.USER))
    }

    @Test
    fun itShouldAddAnewRole() {
        assertThat(user.withAddedRole(Role.MANAGER).roles).isEqualTo("USER,MANAGER")
    }

    @Test
    fun itShouldRemoveARole() {
        assertThat(user.withoutRole(Role.USER).roles).isEmpty()
    }

    @Test
    fun itHasASetOfAuthProviders() {
        assertThat(user.authProviders()).isEqualTo(setOf(AuthProvider.LOCAL))
    }

    @Test
    fun itShouldAddAnewAuthProvider() {
        assertThat(user.withAddedAuthProvider(AuthProvider.GOOGLE).authProviders).isEqualTo("LOCAL,GOOGLE")
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
        assertThat(decoded.authProviders).isEqualTo(user.authProviders)
        assertThat(decoded.isVerified).isEqualTo(user.isVerified)
    }

    @Test
    fun shouldOverrideToString() {
        val uCopy = user.copy(UUID.randomUUID())
        assertThat(uCopy.toString()).isEqualTo("id:${uCopy.id}|email:${uCopy.email}")
    }

    @Test
    fun shouldOverrideHashCode() {
        val uCopy = user.copy(id = UUID.randomUUID())
        assertThat(uCopy.hashCode()).isEqualTo(uCopy.copy().hashCode())
    }

    @Test
    fun shouldOverrideEquals() {
        assertThat(user.copy() == user.copy()).isTrue()
    }

    @Test
    fun shouldAddARefreshToken() {
        assertThat(user.withNewRefreshToken(UUID.randomUUID()).refreshTokens).isNotEmpty()
    }

    @Test
    fun shouldRemoveTheARefreshToken() {
        val uCopy = user.withNewRefreshToken(UUID.randomUUID())
        assertThat(uCopy.withoutRefreshToken(uCopy.refreshTokens.first().token).refreshTokens).isEmpty()
    }


}