package org.example.backend.service

import org.example.backend.model.DoctorBreak
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot

interface DoctorService {
    fun findSchedule(id: Long): List<DoctorWorkingSchedule>
    fun findSlots(id: Long, limit: Int): List<Slot>
    fun findBreak(id:Long):DoctorBreak
    fun findByDoctorIdAndDate(doctorId: Long, date: java.time.LocalDate): List<Slot>
}