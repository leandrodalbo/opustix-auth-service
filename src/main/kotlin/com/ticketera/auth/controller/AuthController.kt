package com.ticketera.auth.controller

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.SignInRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.CREATED)
    fun signIn(@Valid @RequestBody request: SignInRequest) =
        authService.signIn(request)

    @PostMapping("/login")
    fun logIn(@RequestBody request: LoginRequest) = LoginResponse(authService.login(request), "Bearer")

}