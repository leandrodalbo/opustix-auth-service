package com.ticketera.auth.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwtprops")
data class JwtProps(val secret:String, val expiration: Long)
