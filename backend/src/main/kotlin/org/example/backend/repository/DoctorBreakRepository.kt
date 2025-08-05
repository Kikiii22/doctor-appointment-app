package org.example.backend.repository

import org.example.backend.model.DoctorBreak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface DoctorBreakRepository : JpaRepository<DoctorBreak, Long> {
    @Query("SELECT CASE WHEN COUNT(db) > 0 THEN true ELSE false END FROM DoctorBreak db JOIN db.dates d WHERE db.doctor.id = :doctorId AND d = :date")
    fun existsByDoctorAndDate(doctorId: Long, date: LocalDate): Boolean
}
