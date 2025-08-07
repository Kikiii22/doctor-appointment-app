package org.example.backend.api

import org.example.backend.exception.HospitalNotFoundException
import org.example.backend.model.Doctor
import org.example.backend.model.Hospital
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.HospitalRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/hospitals")
class HospitalController(
    private val hospitalRepository: HospitalRepository,
    private val doctorRepository: DoctorRepository
) {
    @GetMapping
    fun getAllHospitals(): ResponseEntity<List<Hospital>> {
        return ResponseEntity.ok(hospitalRepository.findAll())
    }

    @GetMapping("/{id}")
    fun getHospitalById(@PathVariable id: Long): ResponseEntity<Hospital> {
        return ResponseEntity.ok(
            hospitalRepository.findById(id)
                .orElseThrow { HospitalNotFoundException(id) })
    }

    @GetMapping("/{id}/doctors")
    fun getDoctorsByHospital(@PathVariable id: Long): ResponseEntity<List<Doctor>> {
        return ResponseEntity.ok(doctorRepository.findByHospitalId(id))
    }
}