package com.ticketera.auth.integrationtests

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TicketeraAuthApplicationTest : AbstractContainerTest() {


    @LocalServerPort
    private var port: Int = 0

    private lateinit var restClient: RestClient

    @Autowired
    private lateinit var tokenManager: TokenManager

    private val loginRequest = LoginRequest("user@example.com", "0lea@tickets0")

    @BeforeEach
    fun setUp() {
        restClient = RestClient.builder()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun itShouldGetTheUserProfile() {
        val token = loginUser()?.accessToken

        val resp = restClient.get()
            .uri("/profiles/user")
            .header("Authorization", "Bearer $token")
            .retrieve()
            .toEntity(String::class.java)

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(resp.body).isEqualTo("email:user@example.com|roles:USER,ADMIN|authprovider:LOCAL|verified:true")
    }

    @Test
    fun `should login an existing user`() {
        assertThat(loginUser()?.accessToken).isNotEmpty()
    }

    @Test
    fun itShouldRegisterAnewUser() {
        val req = SignInRequest("user@example2.com", "0lea@tickets0")

        val resp = restClient.post()
            .uri("/auth/signin")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.CREATED)
    }

    @Test
    fun loginShouldFailWithInvalidUser() {
        val req = LoginRequest("user@invalidmail.com", "-1")

        assertThatExceptionOfType(HttpClientErrorException::class.java).isThrownBy {
            restClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .toEntity(String::class.java)
        }.withMessageContaining(Message.EMAIL_NOT_FOUND.text)

    }

    private fun loginUser(): LoginResponse? = restClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .body(loginRequest)
        .retrieve()
        .body(LoginResponse::class.java)

}
