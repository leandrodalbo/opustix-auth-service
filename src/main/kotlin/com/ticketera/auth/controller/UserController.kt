package com.ticketera.auth.controller

import com.ticketera.auth.dto.request.UserRoleRequest
import com.ticketera.auth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.ResponseStatus


@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PutMapping("/roles")
    @ResponseStatus(HttpStatus.OK)
    fun setRole(@Valid @RequestBody request: UserRoleRequest) =
        userService.setUserRole(request)

    @DeleteMapping("/delete")
    fun deleteUser() = userService.deleteUser()
}