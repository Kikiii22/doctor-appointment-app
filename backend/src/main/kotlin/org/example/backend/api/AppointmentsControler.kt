package org.example.backend.api

import org.example.backend.dto.CreateAppointmentRequest
import org.example.backend.model.Appointment
import org.example.backend.service.AppointmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/appointments")
class AppointmentsControler(
    private val appointmentService: AppointmentService
) {
    @PostMapping
    fun createAppointment(
        @RequestBody request: CreateAppointmentRequest
    ): ResponseEntity<Appointment> {
        val appointment = appointmentService.bookAppointment(request.slotId, request.patientId)
        return ResponseEntity.ok(appointment)
    }
}
