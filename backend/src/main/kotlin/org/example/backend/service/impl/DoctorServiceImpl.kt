package org.example.backend.service.impl

import org.example.backend.model.DoctorBreak
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot
import org.example.backend.repository.DoctorBreakRepository
import org.example.backend.repository.DoctorScheduleRepository
import org.example.backend.repository.SlotRepository
import org.example.backend.service.DoctorService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DoctorServiceImpl(
    private val doctorScheduleRepository: DoctorScheduleRepository,
    private val slotRepository: SlotRepository,
    private val doctorBreakRepository: DoctorBreakRepository
) : DoctorService {
    override fun findSchedule(id: Long): List<DoctorWorkingSchedule> {
        return doctorScheduleRepository.findByDoctorId(id)
    }

    override fun findSlots(id: Long, limit: Int): List<Slot> {
        return slotRepository.findByDoctorId(id).take(limit)
    }

    override fun findBreak(id: Long): DoctorBreak {
        return doctorBreakRepository.findByDoctorId(id)
    }

    override fun findByDoctorIdAndDate(
        doctorId: Long,
        date: LocalDate
    ): List<Slot> {
        return slotRepository.findByDoctorIdAndDate(doctorId, date)
    }
}