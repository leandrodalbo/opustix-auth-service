package com.ticketera.auth.integrationtests

import com.ticketera.auth.AbstractContainerTest
import com.ticketera.auth.dto.request.*
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.Message
import com.ticketera.auth.jwt.TokenManager
import com.ticketera.auth.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
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
    private val mrDeletionLogin = LoginRequest("deleteuser@example.com", "0lea@tickets0")

    @BeforeEach
    fun setUp() {
        restClient = RestClient.builder()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `should login an existing user`() {
        assertThat(loginUser().body?.accessToken).isNotEmpty()
    }

    @Test
    fun `should refresh the user token`() {
        val login = loginUser()
        val cookie = login.headers.getFirst("set-cookie")

        val resp = restClient.post()
            .uri("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", cookie)
            .retrieve()
            .body(LoginResponse::class.java)
        assertThat(resp?.accessToken).isNotEqualTo(login.body?.accessToken)
    }

    @Test
    fun `should logout`() {
        val login = loginUser()
        val cookie = login.headers.getFirst("set-cookie")

        val resp = restClient.post()
            .uri("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", cookie)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun itShouldSignUpAUser() {
        val req = SignUpRequest("user@example2.com", "Joe Doe", "0lea@tickets0")

        val resp = restClient.post()
            .uri("/auth/signup")
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

    @Test
    fun itShouldVerifyAnewUser() {
        val resp = restClient.get()
            .uri("/auth/verify?token=e4b7f7c4-1d8f-4c02-8d4f-3a8f2109c6fd")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun itShouldAddManagerRoleToTheUser() {
        val login = loginUser()
        val req = UserRoleRequest("user@example.com", Role.MANAGER, UserRoleChange.ADD)
        val resp = restClient.put()
            .uri("/user/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${login.body?.accessToken}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun itShouldDeleteTheUser() {
        val login = restClient.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(mrDeletionLogin)
            .retrieve()
            .toEntity(LoginResponse::class.java)

        val resp = restClient.delete()
            .uri("/user/delete")
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${login.body?.accessToken}")
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    }


    @Test
    fun itShouldSetANewPasswordToken() {
        val req = NewPasswordTokenRequest("user@example.com")
        val resp = restClient.put()
            .uri("/auth/password/token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(req)
            .retrieve()
            .toBodilessEntity()

        assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    }

    private fun loginUser() =
        restClient.post()
            .uri("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(loginRequest)
            .retrieve()
            .toEntity(LoginResponse::class.java)

}
