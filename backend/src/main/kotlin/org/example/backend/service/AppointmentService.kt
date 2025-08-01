package org.example.backend.service

import org.example.backend.model.Slot
import org.example.backend.model.Appointment

interface AppointmentService {
    fun bookAppointment(userId: Long, slotId: Long): Slot
    fun getAppointmentsForUser(userId: Long): List<Appointment>
    fun markAppointmentFinished(appointmentId: Long, treatmentNote: String): Appointment
}
