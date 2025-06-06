package com.ticketera.auth.repository

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
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
        assertThat(user?.authProviders()).contains(AuthProvider.LOCAL)

    }

    @Test
    fun shouldFetchAllUsers() {
        val user = repository?.findAll()?.get(0)

        assertThat(user?.id).isNotNull()
        assertThat(user?.name).isNotNull()
        assertThat(user?.email).isNotNull()
        assertThat(user?.password).isNotNull()
        assertThat(user?.roles()).contains(Role.USER)
        assertThat(user?.authProviders()).contains(AuthProvider.LOCAL)

    }

    @Test
    fun shouldFindAUserByRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = UUID.randomUUID()
            repository?.save(found.withNewRefreshToken(token))

            val updatedUser = repository?.findByRefreshToken(token)
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token
            }).isNotEmpty
        }
    }

    @Test
    fun shouldSaveTheUserRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = UUID.randomUUID()
            repository?.save(found.withNewRefreshToken(token))

            val updatedUser = repository?.findByEmail("user@example.com")
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token
            }).isNotEmpty
        }

    }

    @Test
    fun shouldDeleteTheUserRefreshToken() {
        val user = repository?.findByEmail("user@example.com")

        user?.let { found ->
            val token = UUID.randomUUID()

            val withToken = repository?.save(found.withNewRefreshToken(token))
            withToken?.let {
                repository?.save(it.withoutRefreshToken(token))
            }

            val updatedUser = repository?.findByEmail("user@example.com")
            assertThat(updatedUser?.refreshTokens?.filter {
                it.token == token
            }).isEmpty()
        }
    }

    @Test
    fun shouldFindAUserByPasswordResetToken() {
        val user = repository?.findByPasswordResetToken(UUID.fromString("e4b7f7c4-1d8f-4c02-8d4f-3a8f2109c6fd"))
        assertThat(user?.isPasswordTokenExpired()).isTrue()
    }
}