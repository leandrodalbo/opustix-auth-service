package com.ticketera.auth.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "email")
data class EmailMessage(
    val enabled: Boolean,
    val link: String,
    val from: String,
    val subject: String,
    val message0: String,
    val message1: String,
    val message2: String,
)
