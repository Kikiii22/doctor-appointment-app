package org.example.backend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
data class DoctorBreak(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor? = null,

    @ElementCollection
    val dates: List<LocalDate>? = listOf(),
    val reason: String? = null
)
