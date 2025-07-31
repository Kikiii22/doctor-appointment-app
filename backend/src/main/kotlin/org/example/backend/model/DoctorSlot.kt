package org.example.backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Version
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity
data class DoctorSlot(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    val doctor: Doctor,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(9, 30),
    @Column(nullable = false)
    var isBooked: Boolean = false,
    @Version
@Column(nullable = false)
var version: Long = 0
)
