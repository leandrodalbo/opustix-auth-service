package com.ticketera.auth.repository

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest : AbstractContainerTest() {

    @Autowired
    private val repository: UserRepository? = null

    @Test
    fun shouldCheckUserByEmail() {
        assertThat(repository?.existsByEmail("user@example.com")).isTrue()
    }

    @Test
    fun shouldFindAUserByEmail() {
        val user = repository?.findByEmail("user@example.com")

        assertThat(user?.id).isNotNull()
        assertThat(user?.email).isNotNull()
        assertThat(user?.password).isNotNull()
        assertThat(user?.roles()).contains(Role.USER)
        assertThat(user?.authProvider).isEqualTo(AuthProvider.LOCAL)

    }

    @Test
    fun shouldAllUsers() {
        val user = repository?.findAll()?.get(0)

        assertThat(user?.id).isNotNull()
        assertThat(user?.email).isNotNull()
        assertThat(user?.password).isNotNull()
        assertThat(user?.roles()).contains(Role.USER)
        assertThat(user?.authProvider).isEqualTo(AuthProvider.LOCAL)

    }

    @Test
    fun shouldFindAUserByRefreshToken() {
        val user = repository?.findByRefreshToken(UUID.fromString("8f2d7c4a-3a09-4f2e-9c5b-71e65d24f5b3"))

        assertThat(user?.id).isNotNull()
        assertThat(user?.refreshToken).isEqualTo(UUID.fromString("8f2d7c4a-3a09-4f2e-9c5b-71e65d24f5b3"))

    }
}