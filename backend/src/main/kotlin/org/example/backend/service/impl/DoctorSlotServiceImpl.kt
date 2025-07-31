package org.example.backend.service.impl

import org.example.backend.model.DoctorSlot
import org.example.backend.repository.DoctorSlotRepository
import org.example.backend.service.DoctorSlotService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class DoctorSlotServiceImpl(
    private val slotRepo: DoctorSlotRepository
) : DoctorSlotService {

    override fun getEarliestAvailableSlot(doctorId: Long): DoctorSlot? {
        return slotRepo.findByDoctorIdAndDateBetweenAndIsBookedFalse(
            doctorId,
            LocalDate.now(),
            LocalDate.now().plusDays(30)
        ).minByOrNull { LocalDateTime.of(it.date, it.startTime) }
    }

    override fun getAvailableSlotsByDate(doctorId: Long, date: LocalDate): List<DoctorSlot> {
        return slotRepo.findByDoctorIdAndDateBetweenAndIsBookedFalse(
            doctorId,
            date,
            date
        )
    }
}
