package org.example.backend.dto

data class CreateAppointmentRequest (val slotId: Long,
                                     val patientId: Long)