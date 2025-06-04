package com.ticketera.auth.repository

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.RefreshToken
import com.ticketera.auth.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant
import java.util.UUID

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
        assertThat(user?.name).isNotNull()
        assertThat(user?.password).isNotNull()
        assertThat(user?.roles()).contains(Role.USER)
        assertThat(user?.authProvider).isEqualTo(AuthProvider.LOCAL)

    }

    @Test
    fun shouldAllUsers() {
        val user = repository?.findAll()?.get(0)

        assertThat(user?.id).isNotNull()
        assertThat(user?.name).isNotNull()
        assertThat(user?.email).isNotNull()
        assertThat(user?.password).isNotNull()
        assertThat(user?.roles()).contains(Role.USER)
        assertThat(user?.authProvider).isEqualTo(AuthProvider.LOCAL)

    }

    @Test
    fun shouldFindAUserByRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = RefreshToken(null, UUID.randomUUID(), Instant.now().toEpochMilli(), user = user)
            found.refreshTokens.add(token)
            repository?.save(found)

            val updatedUser = repository?.findByRefreshToken(token.token)

            assertThat(user.email).isEqualTo("user@example.com")
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token.token
            }).isNotEmpty
        }

    }

    @Test
    fun shouldSaveTheUserRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = RefreshToken(null, UUID.randomUUID(), Instant.now().toEpochMilli(), user = user)
            found.refreshTokens.add(token)
            repository?.save(found)

            val updatedUser = repository?.findByEmail("user@example.com")
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token.token
            }).isNotEmpty
        }

    }

    @Test
    fun shouldDeleteTheUserRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = RefreshToken(null, UUID.randomUUID(), Instant.now().toEpochMilli(), user = user)
            found.refreshTokens.add(token)

            val withToken = repository?.save(found)
            withToken?.let {
                it.refreshTokens.remove(withToken.refreshTokens[0])
                repository?.save(it)
            }

            val updatedUser = repository?.findByEmail("user@example.com")
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token.token
            }).isEmpty()
        }
    }
}