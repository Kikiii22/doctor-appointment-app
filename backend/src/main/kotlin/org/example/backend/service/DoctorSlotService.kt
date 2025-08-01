package org.example.backend.service

import org.example.backend.model.Slot
import java.time.LocalDate

interface DoctorSlotService {
    fun getEarliestAvailableSlot(doctorId: Long): Slot?
    fun getAvailableSlotsByDate(doctorId: Long, date: LocalDate): List<Slot>
}
