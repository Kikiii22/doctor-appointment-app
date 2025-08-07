package org.example.backend.api

import org.example.backend.model.Department
import org.example.backend.repository.DepartmentRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/departments")
class DepartmentsController (private val departmentRepository: DepartmentRepository){
    fun getAllDepartments(): List<Department> {
        return departmentRepository.findAll()
    }
}