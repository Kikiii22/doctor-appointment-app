package org.example.backend.model

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
data class DoctorWorkingSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor? = null,

    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    val startTime: LocalTime? = LocalTime.of(9, 0),
    val endTime: LocalTime? = LocalTime.of(17, 0),
    val breakStart: LocalTime? = LocalTime.of(13, 0),
    val breakEnd: LocalTime? = LocalTime.of(14, 0),
    val isWorking: Boolean = true
)
