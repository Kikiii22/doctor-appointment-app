package org.example.backend.model
import jakarta.persistence.*

@Entity
data class Department (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", unique = true)
    val name: String = "",

    val description: String = ""
)