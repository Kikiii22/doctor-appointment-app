package org.example.backend.repository
import org.example.backend.model.Doctor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID


interface DoctorRepository : JpaRepository<Doctor, Long> {
    fun findBySpecializationContainingIgnoreCase(specialization: String): List<Doctor>
    fun findByFullNameContainingIgnoreCase(name: String): List<Doctor>
    //fun findByHospital_NameContainingIgnoreCase(hospitalName: String): List<Doctor>
}
