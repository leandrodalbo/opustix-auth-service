package com.ticketera.auth.repository

import com.ticketera.auth.AbstractContainerTest
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
class VerifyUserRepositoryTest : AbstractContainerTest() {

    @Autowired
    private val repository: VerifyUserRepository? = null

    @Test
    fun shouldFindById() {
        val verifyUser = repository?.findById(UUID.fromString("e4b7f7c4-1d8f-4c02-8d4f-3a8f2109c6fd"))

        assertThat(verifyUser?.get()?.email).isEqualTo("user@example.com")
        assertThat(verifyUser?.get()?.isExpired()).isTrue()
    }

    @Test
    fun shouldFindByEmail() {
        val verifyUser = repository?.findByEmail("user@example.com")

        assertThat(verifyUser?.email).isEqualTo("user@example.com")
    }
}