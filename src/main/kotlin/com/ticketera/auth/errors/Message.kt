package com.ticketera.auth.errors

enum class Message(val text: String) {
    EMAIL_IN_USE("Email already in use"),
    REQUEST_FAILED("Request Failed");

    override fun toString(): String = text
}
