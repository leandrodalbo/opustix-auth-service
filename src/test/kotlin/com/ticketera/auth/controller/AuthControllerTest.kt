package com.ticketera.auth.controller

import com.ninjasquad.springmockk.MockkBean
import com.ticketera.auth.conf.SecurityConfig
import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.service.AuthService
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var authService: AuthService

    private val signInRequest = SignInRequest("user@email.com", "1aads@34b")
    private val loginRequest = LoginRequest("user@email.com", "1aads@34b")

    private val objectMapper = ObjectMapper()

    @Test
    fun `should register a new user`() {
        val expectedUuid = UUID.randomUUID()

        every { authService.signIn(signInRequest) } returns expectedUuid

        mockMvc.post("/auth/signin")
        {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signInRequest)
        }.andExpect {
            status { isOk() }
            content { expectedUuid }
        }


        verify { authService.signIn(signInRequest) }
    }

    @Test
    fun `should fail with invalid email or pass`() {
        val invalidData = SignInRequest("notemail", "234notsafe")

        mockMvc.post("/auth/signin")
        {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidData)
        }.andExpect {
            status { is5xxServerError() }
            content { "Request Failed" }
        }

    }

    @Test
    fun `should login`() {

        every { authService.login(any()) } returns "anew234generated457token"

        mockMvc.post("/auth/login")
        {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            content { objectMapper.writeValueAsString(LoginResponse("anew234generated457token", "Bearer")) }
        }


        verify { authService.login(any()) }
    }

}