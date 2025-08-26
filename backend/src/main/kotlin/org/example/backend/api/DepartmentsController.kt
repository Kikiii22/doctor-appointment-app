package org.example.backend.api

import org.example.backend.dto.DepartmentResponse
import org.example.backend.exception.HospitalNotFoundException
import org.example.backend.model.Department
import org.example.backend.model.Doctor
import org.example.backend.repository.DepartmentRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.service.embedding.EmbeddingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/departments")
class DepartmentsController(
    private val departmentRepository: DepartmentRepository,
    private val embeddingService: EmbeddingService,
    private val doctorRepository: DoctorRepository
) {
    @GetMapping
    fun getAllDepartments(): List<Department> {
        return departmentRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getDepartmentById(@PathVariable id: Long): ResponseEntity<Department> {
        return ResponseEntity.ok(
            departmentRepository.findById(id)
                .orElseThrow { HospitalNotFoundException(id) })
    }


    @GetMapping("/search")
    fun searchDoctorsBySymptoms(@RequestParam("symptoms") symptoms: String): List<Doctor> {
        val topDepartments = embeddingService.findRelevantDepartments(symptoms)
            .sortedByDescending { it.second }
            .map { it.first }

        val doctors = mutableListOf<Doctor>()
        for (deptName in topDepartments) {
            val deptDoctors = doctorRepository.findByDepartmentName(deptName)
            doctors.addAll(deptDoctors)
        }
        return doctors
    }
}