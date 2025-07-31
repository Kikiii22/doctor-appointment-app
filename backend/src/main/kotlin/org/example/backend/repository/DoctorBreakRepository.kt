package org.example.backend.repository
import org.example.backend.model.Doctor
import org.example.backend.model.DoctorBreak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID


interface DoctorBreakRepository : JpaRepository<DoctorBreak, Long> {
    fun existsByDoctorAndDate(doctor: Doctor, date: LocalDate): Boolean

    fun findAllByDoctorAndDateBetween(doctor: Doctor, from: LocalDate, to: LocalDate): List<DoctorBreak>
}
