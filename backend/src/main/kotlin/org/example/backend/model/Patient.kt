package org.example.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "patients")
data class Patient(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(name = "full_name")
    val fullName: String = "",

    val phone: String = "",

    val email: String = "",

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User()
)
