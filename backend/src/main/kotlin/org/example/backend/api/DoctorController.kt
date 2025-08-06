package org.example.backend.api

import org.example.backend.exception.DoctorNotFoundException
import org.example.backend.model.*
import org.example.backend.repository.DoctorRepository
import org.example.backend.service.AppointmentService
import org.example.backend.service.DoctorService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/doctors")
class DoctorController(
    private val doctorService: DoctorService,
    private val doctorRepository: DoctorRepository,
    private val appointmentService: AppointmentService
) {
    @GetMapping
    fun getAllDoctors(): ResponseEntity<List<Doctor>> {
        return ResponseEntity.ok(doctorRepository.findAll())
    }

    @GetMapping("/{id}")
    fun getDoctorById(@PathVariable id: Long): ResponseEntity<Doctor> {
        return ResponseEntity.ok(
            doctorRepository.findById(id)
                .orElseThrow { DoctorNotFoundException(id) })
    }

    @GetMapping("/{id}/appointments")
    fun getDoctorAppointments(@PathVariable id: Long): ResponseEntity<List<Appointment>> {
        return ResponseEntity.ok(appointmentService.findAppointmentsByDoctor(id))
    }

    @GetMapping("/{id}/schedule")
    fun getDoctorSchedule(@PathVariable id: Long): ResponseEntity<List<DoctorWorkingSchedule>> {
        return ResponseEntity.ok(doctorService.findSchedule(id))
    }

    //TO DO show slots only after the local time for today
    @GetMapping("/{id}/slots")
    fun getDoctorSlots(
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = "28")
        limit: Int,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<List<Slot>> {
        val slots = if (date != null) {
            doctorService.findByDoctorIdAndDate(id, date)
        } else {
            doctorService.findSlots(id, limit)
        }
        return ResponseEntity.ok(slots)
    }

    @GetMapping("/{id}/break")
    fun getDoctorBreak(@PathVariable id: Long): ResponseEntity<DoctorBreak?> {
        val doctorBreak = doctorService.findBreak(id)
        return if (doctorBreak != null) {
            ResponseEntity.ok(doctorBreak)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}