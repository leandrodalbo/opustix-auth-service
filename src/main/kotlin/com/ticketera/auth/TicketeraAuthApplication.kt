package com.ticketera.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TicketeraAuthApplication

fun main(args: Array<String>) {
    runApplication<TicketeraAuthApplication>(*args)
}
