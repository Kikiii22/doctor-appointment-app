package org.example.backend.repository

import org.example.backend.model.Department
import org.springframework.data.jpa.repository.JpaRepository

interface DepartmentRepository : JpaRepository<Department, Long> {
    fun findByName(name: String)
}