package org.example.backend.api

import org.example.backend.dto.AppointmentRequest
import org.example.backend.model.Appointment
import org.example.backend.service.AppointmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/appointments")
class AppointmentsController(
    private val appointmentService: AppointmentService
) {
    @PostMapping("/book")
    fun createAppointment(
        @RequestBody request: AppointmentRequest
    ): ResponseEntity<Appointment> {
        val appointment = appointmentService.bookAppointment(request.slotId, request.patientId)
        return ResponseEntity.ok(appointment)
    }

    @PostMapping("/cancel")
    fun cancelAppointment(
        @RequestBody request: AppointmentRequest
    ): ResponseEntity<Void> {
        appointmentService.cancelAppointment(request.slotId, request.patientId)
        return ResponseEntity.ok().build()
    }
}
