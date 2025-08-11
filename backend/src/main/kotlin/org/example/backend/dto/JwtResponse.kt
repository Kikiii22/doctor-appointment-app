package org.example.backend.dto

data class JwtResponse(
    val token: String,
    val user: UserDto
)