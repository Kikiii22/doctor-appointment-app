package org.example.backend.api

import org.example.backend.model.Appointment
import org.example.backend.repository.AppointmentRepository
import org.example.backend.service.PatientService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/patients")
class PatientController(
    val appointmentRepository: AppointmentRepository,
    val patientService: PatientService
) {

    @GetMapping("/{id}/appointments")
    fun getAppointmentsByPatient(@PathVariable id: Long): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(id))
    }

}