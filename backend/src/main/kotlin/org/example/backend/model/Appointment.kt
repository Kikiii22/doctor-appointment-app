package org.example.backend.model
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.EnumType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToOne
import java.util.UUID
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import jakarta.persistence.Id
import jakarta.persistence.OneToOne


@Entity
data class Appointment(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor,

    @OneToOne
    @JoinColumn(name = "slot_id")
    val slot: DoctorSlot,

    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    var treatmentNote: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
