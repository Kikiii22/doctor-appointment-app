package org.example.backend.api

import org.example.backend.exception.HospitalNotFoundException
import org.example.backend.model.Department
import org.example.backend.model.Hospital
import org.example.backend.repository.DepartmentRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/departments")
class DepartmentsController (private val departmentRepository: DepartmentRepository){
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
}