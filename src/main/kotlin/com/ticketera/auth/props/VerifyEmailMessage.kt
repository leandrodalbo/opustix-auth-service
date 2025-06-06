package com.ticketera.auth.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "email")
data class VerifyEmailMessage(
    val enabled: Boolean,
    val link: String,
    val from: String,
    val subject0: String,
    val message0: String,
    val subject1: String,
    val message1: String,
    val subject2: String,
    val message2: String,
    val subject3: String,
    val message3: String,
    val subject4: String,
    val message4: String,
    val closing: String
)
