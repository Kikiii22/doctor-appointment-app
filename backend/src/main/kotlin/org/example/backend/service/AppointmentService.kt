package org.example.backend.service

import org.example.backend.model.Appointment

interface AppointmentService {
    fun findAppointmentsByDoctor(id: Long): List<Appointment>
    fun findAppointmentsByPatient(id: Long): List<Appointment>
    fun findAppointmentsInHospital(id: Long): List<Appointment>
    fun findAppointmentsByDepartment(id: Long): List<Appointment>
}