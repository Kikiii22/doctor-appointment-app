package org.example.backend.service

import org.example.backend.model.Appointment

interface AppointmentService {
    fun findAppointmentsByDoctor(id: Long): List<Appointment>
    fun findAppointmentsByPatient(id: Long): List<Appointment>
    fun findAppointmentsInHospital(id: Long): List<Appointment>
    fun findAppointmentsByDepartment(id: Long): List<Appointment>
    fun bookAppointment(slotId: Long, patientId: Long): Appointment
    fun cancelAppointment(slotId: Long, patientId: Long)
    fun finishAppointment(appointmentId: Long, doctorId: Long,description:String): Appointment
}