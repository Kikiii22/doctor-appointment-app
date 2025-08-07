package org.example.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val username: String,
    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val password: String,
    val email: String,
    @field:NotBlank
    @field:Size(max = 50)
    val fullName: String,
    val phone: String

)