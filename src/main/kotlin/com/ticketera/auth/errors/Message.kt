package com.ticketera.auth.errors

enum class Message(val text: String) {
    EMAIL_IN_USE("Email already in use");

    override fun toString(): String = text
}
