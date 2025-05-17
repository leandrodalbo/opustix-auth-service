package com.ticketera.auth.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.sql.SQLException

@RestControllerAdvice
class CustomErrorHandler {

    @ExceptionHandler(SQLException::class, Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Message.REQUEST_FAILED.text)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleInvalidArgument(ex: Exception): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)
    }

    @ExceptionHandler(InvalidUserException::class)
    fun handleInvalidUser(ex: Exception): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.message)
    }
}