package org.example.backend.service

import org.example.backend.model.Appointment
import java.util.UUID

interface AppointmentService {
    fun bookAppointment(userId: Long, slotId: Long): Appointment
    fun getAppointmentsForUser(userId: Long): List<Appointment>
    fun markAppointmentFinished(appointmentId: Long, treatmentNote: String): Appointment
}
