package org.example.backend.dto

data class AuthUserDto(val id: Long, val username: String, val roles: List<String>)
