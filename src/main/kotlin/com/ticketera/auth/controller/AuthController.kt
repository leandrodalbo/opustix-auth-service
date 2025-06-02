package com.ticketera.auth.controller

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.RefreshTokenRequest
import com.ticketera.auth.dto.request.SignUpRequest
import com.ticketera.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest) =
        authService.signUp(request)

    @PostMapping("/login")
    fun logIn(@RequestBody request: LoginRequest) = authService.login(request)

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest) = authService.refresh(request)

    @PostMapping("/logout")
    fun logout(@RequestBody request: RefreshTokenRequest) = authService.logout(request)

    @GetMapping("/verify")
    fun verify(@RequestParam token: String) =
        authService.verifyUser(token)
}