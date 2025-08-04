package org.example.backend.repository

import org.example.backend.model.DoctorWorkingSchedule
import org.springframework.data.jpa.repository.JpaRepository

interface DoctorScheduleRepository : JpaRepository<DoctorWorkingSchedule, Long> {
    fun findByDoctorId(id: Long): DoctorWorkingSchedule
}