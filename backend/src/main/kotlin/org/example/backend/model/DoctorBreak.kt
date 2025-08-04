package org.example.backend.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class DoctorBreak(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor? = null,

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val reason: String? = null
)
