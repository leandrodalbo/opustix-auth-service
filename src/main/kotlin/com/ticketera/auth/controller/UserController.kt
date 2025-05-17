package com.ticketera.auth.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/profiles")
class UserController {

    @GetMapping("/user")
    fun user(@AuthenticationPrincipal user: UserDetails): String = user.username

}