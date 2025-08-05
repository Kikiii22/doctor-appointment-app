package org.example.backend.model

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name="schedule")
data class DoctorWorkingSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor = Doctor(),

    val date: LocalDate = LocalDate.of(2025,6,20),

    @Column(name="start_time")
    val startTime: LocalTime? = LocalTime.of(9, 0),

    @Column(name="end_time")
    val endTime: LocalTime? = LocalTime.of(17, 0),

    @Column(name="break_start")
    val breakStart: LocalTime? = LocalTime.of(13, 0),

    @Column(name="break_end")
    val breakEnd: LocalTime? = LocalTime.of(14, 0),

    @Column(name="is_working")
    val isWorking: Boolean = true
)
