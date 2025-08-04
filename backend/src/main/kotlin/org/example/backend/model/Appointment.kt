package org.example.backend.model

import jakarta.persistence.*

@Entity
class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "slot_id", referencedColumnName = "id")
    val slot: Slot,

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    val patient: Patient,

    val status: AppointmentStatus = AppointmentStatus.AVAILABLE,

    val description: String = "",
)
