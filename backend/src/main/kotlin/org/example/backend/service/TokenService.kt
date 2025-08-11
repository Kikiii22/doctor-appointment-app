package org.example.backend.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.example.backend.config.JwtProperties
import org.example.backend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService
    (jwtProperties: JwtProperties, private val userRepository: UserRepository, repository: UserRepository) {
    private val secretKey = Keys.hmacShaKeyFor(jwtProperties.key.key.toByteArray())

    fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {

        return Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date())
            .setExpiration(expirationDate)
            .addClaims(additionalClaims)
            .signWith(secretKey)
            .compact()
    }

    fun isExpired(token: String): Boolean {
        return getAllClaims(token).expiration.before(Date(System.currentTimeMillis()))
    }

    fun getUsername(token: String): String? {
        return getAllClaims(token).subject
    }

    fun isValid(token: String, userDetails: UserDetails): Boolean {
        return getUsername(token) == userDetails.username && !isExpired(token)
    }

    fun getAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body


    }

}