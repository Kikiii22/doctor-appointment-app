package org.example.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val key: Secret,
    val accessTokenExpiration: Token,
    val refreshTokenExpiration: Refresh
){
    data class Secret(val key: String)
    data class Token(val expiration: Long)
    data class Refresh(val expiration: Long)
}