package org.example.backend.api

import org.example.backend.dto.AppointmentRequest
import org.example.backend.dto.FinishAppointmentRequest
import org.example.backend.model.Appointment
import org.example.backend.service.AppointmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = ["http://localhost:4200"])
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

    @PatchMapping("/{id}/finish")
    fun finishAppointment(
        @PathVariable id: Long, @RequestBody request: FinishAppointmentRequest,
        @RequestParam doctorId: Long
    )
            : ResponseEntity<Any> {
        try {
            val appointment = appointmentService.finishAppointment(id, doctorId, request.description)
            return ResponseEntity.ok(appointment)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(null)
        }
    }


    @PostMapping("/cancel")
    fun cancelAppointment(
        @RequestBody request: AppointmentRequest
    ): ResponseEntity<Void> {
        appointmentService.cancelAppointment(request.slotId, request.patientId)
        return ResponseEntity.ok().build()
    }
}
