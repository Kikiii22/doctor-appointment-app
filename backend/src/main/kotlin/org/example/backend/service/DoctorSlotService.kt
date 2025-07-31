package org.example.backend.service

import org.example.backend.model.DoctorSlot
import java.time.LocalDate
import java.util.UUID

interface DoctorSlotService {
    fun getEarliestAvailableSlot(doctorId: Long): DoctorSlot?
    fun getAvailableSlotsByDate(doctorId: Long, date: LocalDate): List<DoctorSlot>
}
