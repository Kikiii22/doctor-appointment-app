package org.example.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    val username: String,
    val password: String,

    @Enumerated(EnumType.STRING)
    val role: Role = Role.PATIENT
)
