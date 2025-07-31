package org.example.backend.repository
import org.example.backend.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByDoctorIdAndSlot_Date(doctorId: Long, date: LocalDate): List<Appointment>
    fun findByUserId(userId: Long): List<Appointment>
}
