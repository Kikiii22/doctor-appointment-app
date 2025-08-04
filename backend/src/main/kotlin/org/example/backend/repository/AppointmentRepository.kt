package org.example.backend.repository

import org.example.backend.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByPatientId(id: Long): List<Appointment>
    fun findBySlotDoctorId(id: Long): List<Appointment>
    fun findBySlotDoctorHospitalId(id: Long): List<Appointment>
    fun findBySlotDoctorDepartmentId(id: Long): List<Appointment>
}