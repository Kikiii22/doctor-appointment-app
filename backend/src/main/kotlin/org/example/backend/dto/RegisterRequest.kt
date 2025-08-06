package org.example.backend.dto

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val fullName: String,
    val phone: String
)