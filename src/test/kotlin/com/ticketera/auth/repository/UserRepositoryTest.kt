package com.ticketera.auth.repository

import com.ticketera.auth.model.AuthProvider
import com.ticketera.auth.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class UserRepositoryTest {
    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer("postgres:16.4").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    @Autowired
    private val repository: UserRepository? = null

    @Test
    fun shouldCheckUserByEmail() {
        assertThat(repository?.existsByEmail("user@example.com")).isTrue()
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
}