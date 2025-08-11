package org.example.backend.dto

import org.example.backend.model.Role

data class UserDto(
    val id: Long,
    val username: String,
    val role: Role,
    val email: String?
)