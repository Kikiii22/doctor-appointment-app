package org.example.backend.repository

import org.example.backend.model.DoctorSlot
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface DoctorSlotRepository : JpaRepository<DoctorSlot, Long> {

    fun findByDoctorIdAndDateBetweenAndIsBookedFalse(
        doctorId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<DoctorSlot>

    fun existsByDoctorIdAndDate(doctorId: Long, date: LocalDate): Boolean

    fun deleteAllByDateBeforeAndIsBookedFalse(date: LocalDate)
}