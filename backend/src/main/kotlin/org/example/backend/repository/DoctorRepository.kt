package org.example.backend.repository

import org.example.backend.model.Department
import org.example.backend.model.Doctor
import org.springframework.data.jpa.repository.JpaRepository

interface DoctorRepository : JpaRepository<Doctor, Long> {
    fun findByFullName(fullName: String)
    fun findByDepartment(department: Department)
    fun findByHospitalId(id: Long): List<Doctor>
}