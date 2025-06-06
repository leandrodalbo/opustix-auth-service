package com.ticketera.auth.errors

enum class Message(val text: String) {
    EMAIL_IN_USE("Email already in use"),
    EMAIL_NOT_FOUND("User Email not found"),
    INVALID_PASSWORD("Invalid User password"),
    REQUEST_FAILED("Request Failed"),
    INVALID_TOKEN("Invalid Token"),
    EMAIL_SERVICE_FAILED("Email Service Failed"),
    VERIFY_SERVICE_FAILED("Verify Service Failed"),
    USER_NOT_VERIFIED("User Not Verified"),
    NOT_ADMIN_USER("Not Admin User"),
    USER_ALREADY_CONTAINS_THE_ROLE("The User Already has the role"),
    USER_DOES_NOT_CONTAIN_THE_ROLE("The User Does not have the role");

    override fun toString(): String = text
}
