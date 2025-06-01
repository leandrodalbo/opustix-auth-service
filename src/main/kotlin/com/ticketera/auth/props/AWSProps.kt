package com.ticketera.auth.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws")
data class AWSProps(val region:String, val key:String, val secret:String)
