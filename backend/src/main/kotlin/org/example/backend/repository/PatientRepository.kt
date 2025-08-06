package org.example.backend.repository

import org.example.backend.model.Patient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientRepository : JpaRepository<Patient, Long> {
    fun findByFullName(fullName: String): Patient?
    fun existsByUserEmail(email: String): Boolean
}
