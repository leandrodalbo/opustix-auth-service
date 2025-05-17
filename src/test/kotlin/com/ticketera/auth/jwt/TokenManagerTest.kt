package com.ticketera.auth.jwt

import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import com.ticketera.auth.model.User
import com.ticketera.auth.props.JwtProps
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TokenManagerTest {

    private val jwtProps =
        JwtProps("9RUNnGEu9PNYKQtl+EHpYGBb4Lt5hpA76nsIrNTsTx133eLHJ/R6YyMKq6CS48cUWfbRcy+ZYs/qRMIVu8oewQ==", 3600000)

    private val user = User(
        null,
        "user@mail.com",
        "a32dga34adfv34",
        Role.USER.name,
        AuthProvider.LOCAL,
        false
    )

    private val tokenManager = TokenManager(jwtProps)

    @Test
    fun shouldGenerateANewToken() {
        val result = tokenManager.generateToken(user)
        assertThat(result).isNotNull()
    }

    @Test
    fun shouldGetUserInfo() {
        val result = tokenManager.getUserInfo(tokenManager.generateToken(user))
        assertThat(result).isEqualTo(user.tokenString())
    }

    @Test
    fun shouldFailWithInvalidUserInfo() {
        val result = tokenManager.getUserInfo("somerandomstring2352252")
        assertThat(result).isEqualTo("invalid-token")
    }


    @Test
    fun shouldValidateAUserToken() {
        val result = tokenManager.isAValidToken(
            tokenManager.generateToken(user)
        )
        assertThat(result).isTrue()
    }

    @Test
    fun shouldBeFalseWithInvalidToken() {
        val result = tokenManager.isAValidToken("somerandomstring2352252")
        assertThat(result).isFalse()
    }


    @Test
    fun shouldGetUserEmailFromToken() {
        val result = tokenManager.getUserEmail(tokenManager.generateToken(user))
        assertThat(result).isEqualTo("user@mail.com")
    }
}