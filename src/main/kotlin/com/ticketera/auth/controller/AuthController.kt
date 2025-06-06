package com.ticketera.auth.controller

import com.ticketera.auth.dto.request.LoginRequest
import com.ticketera.auth.dto.request.NewPasswordTokenRequest
import com.ticketera.auth.dto.request.SignUpRequest
import com.ticketera.auth.dto.response.LoginResponse
import com.ticketera.auth.errors.InvalidUserException
import com.ticketera.auth.errors.Message
import com.ticketera.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
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
    fun logIn(@RequestBody request: LoginRequest, response: HttpServletResponse): LoginResponse {
        val pair = authService.login(request)
        response.addCookie(pair.cookie)
        return pair.loginResponse
    }

    @PostMapping("/refresh")
    fun refresh(request: HttpServletRequest, response: HttpServletResponse): LoginResponse {
        val cookie = request.cookies?.firstOrNull { it.name == "refreshToken" }
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)

        val pair = authService.refresh(cookie.value)

        response.addCookie(pair.cookie)

        return pair.loginResponse
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        val cookie = request.cookies?.firstOrNull { it.name == "refreshToken" }
            ?: throw InvalidUserException(Message.INVALID_TOKEN.text)

        authService.logout(cookie.value)
    }

    @GetMapping("/verify")
    fun verify(@RequestParam token: String) =
        authService.verifyUser(token)

    @PutMapping("/password/token")
    fun passwordToken(@Valid @RequestBody newPasswordTokenRequest: NewPasswordTokenRequest) =
        authService.setPasswordToken(newPasswordTokenRequest)
}