package org.example.backend.repository

import org.example.backend.model.Hospital
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID


@Repository
interface HospitalRepository : JpaRepository<Hospital, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Hospital>
}
