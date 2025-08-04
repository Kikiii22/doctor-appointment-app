package org.example.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "hospital")
data class Hospital(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val email: String,
    @Column(nullable = false)
    val phone: String,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
)
