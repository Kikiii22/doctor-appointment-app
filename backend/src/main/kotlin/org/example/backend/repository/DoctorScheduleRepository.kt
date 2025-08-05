package org.example.backend.repository

import org.example.backend.model.DoctorWorkingSchedule
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek

interface DoctorScheduleRepository : JpaRepository<DoctorWorkingSchedule, Long> {
    fun findByDoctorId(id: Long): List<DoctorWorkingSchedule>
    fun findByDoctorIdAndDayOfWeek(id: Long, dayOfWeek: DayOfWeek): DoctorWorkingSchedule?
}