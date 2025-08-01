package org.example.backend.repository

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.model.Slot
import org.example.backend.model.Patient
import org.springframework.data.jpa.repository.JpaRepository

interface AppointmentRepository: JpaRepository<Appointment, Long> {
    fun findByPatient(patient: Patient)
    fun findBySlot(slot: Slot)
    fun findByPatientAndStatus(patient: Patient, status: AppointmentStatus)
    fun findByStatus(status: AppointmentStatus)
}