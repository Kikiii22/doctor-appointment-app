package org.example.backend.repository

import org.example.backend.model.DoctorWorkingSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.DayOfWeek
import java.util.UUID


@Repository
interface DoctorWorkingScheduleRepository : JpaRepository<DoctorWorkingSchedule, Long> {
    fun findByDoctorIdAndDayOfWeek(doctorId: Long, dayOfWeek: DayOfWeek): DoctorWorkingSchedule?
}
