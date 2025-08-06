package org.example.backend.model

import jakarta.persistence.*

@Table(name = "appointments")
@Entity
class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "slot_id", referencedColumnName = "id")
    val slot: Slot = Slot(),

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    val patient: Patient = Patient(),

    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus = AppointmentStatus.AVAILABLE,

    var description: String = "",
)
