package org.example.backend.api

import org.example.backend.model.Appointment
import org.example.backend.model.Patient
import org.example.backend.repository.AppointmentRepository
import org.example.backend.service.PatientService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.example.backend.repository.PatientRepository


@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/patients")
class PatientController(
    val appointmentRepository: AppointmentRepository,
    val patientService: PatientService,
    val patientRepository: PatientRepository
) {

    @GetMapping("/{id}/appointments")
    fun getAppointmentsByPatient(@PathVariable id: Long): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(id))
    }

    @GetMapping("/user/{id}")
    fun getPatientByUserId(@PathVariable id: Long): ResponseEntity<Patient?> {
        return ResponseEntity.ok(patientRepository.findByUserId(id))
    }

}