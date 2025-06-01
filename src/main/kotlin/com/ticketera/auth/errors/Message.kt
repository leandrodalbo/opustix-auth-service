package com.ticketera.auth.errors

enum class Message(val text: String) {
    EMAIL_IN_USE("Email already in use"),
    EMAIL_NOT_FOUND("User Email not found"),
    INVALID_PASSWORD("Invalid User password"),
    REQUEST_FAILED("Request Failed"),
    INVALID_TOKEN("Invalid Token"),
    EMAIL_SERVICE_FAILED("Email Service Failed");
    

    override fun toString(): String = text
}
