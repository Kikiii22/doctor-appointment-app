package org.example.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "patients")
data class Patient(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    val fullName: String = "",

    val phone: String = "",

    val email: String = "",

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
)
