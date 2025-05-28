package com.ticketera.auth.integrationtests

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
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


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
        "spring.security.oauth2.client.registration.google.client-id=fake",
        "spring.security.oauth2.client.registration.google.client-secret=fake"
    ]
)
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
        val user = loginUser()

        val resp = restClient.get()
            .uri("/profiles/user")
            .header("Authorization", "Bearer ${user?.accessToken}")
            .retrieve()
            .toEntity(String::class.java)

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(resp.body).isNotNull()
    }

    @Test
    fun itShouldGetTheUserProfileAfterLogout() {
        val user = loginUser()
        val req = RefreshTokenRequest(user?.refreshToken!!)

        val logoutResponse = restClient.post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .toBodilessEntity()


        assertThatExceptionOfType(HttpClientErrorException::class.java)
            .isThrownBy {
                restClient.get()
                    .uri("/profiles/user")
                    .header("Authorization", "Bearer ${user.accessToken}")
                    .retrieve()
                    .toEntity(String::class.java)
            }.withMessageContaining("401")

        assertThat(logoutResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should login an existing user`() {
        assertThat(loginUser()?.accessToken).isNotEmpty()
    }

    @Test
    fun `should refresh the token`() {
        val login = loginUser()
        val req = RefreshTokenRequest(login?.refreshToken!!)

        val resp = restClient.post()
            .uri("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .body(LoginResponse::class.java)

        assertThat(resp?.accessToken).isNotEqualTo(login.accessToken)

    }

    @Test
    fun `should logout`() {
        val login = loginUser()
        val req = RefreshTokenRequest(login?.refreshToken!!)

        val resp = restClient.post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)

    }

    @Test
    fun itShouldRegisterAnewUser() {
        val req = SignInRequest("user@example2.com", "Joe Doe", "0lea@tickets0")

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
