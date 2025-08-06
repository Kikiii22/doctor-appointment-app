package org.example.backend

import org.example.backend.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(JwtProperties::class)

class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
