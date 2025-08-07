package org.example.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val password: String
)

