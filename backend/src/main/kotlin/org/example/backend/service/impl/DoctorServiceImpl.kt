package org.example.backend.service.impl

import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot
import org.example.backend.repository.DoctorScheduleRepository
import org.example.backend.repository.SlotRepository
import org.example.backend.service.DoctorService
import org.springframework.stereotype.Service

@Service
class DoctorServiceImpl(
    private val doctorScheduleRepository: DoctorScheduleRepository,
    private val slotRepository: SlotRepository
) : DoctorService {
    override fun findSchedule(id: Long): List<DoctorWorkingSchedule> {
        return doctorScheduleRepository.findByDoctorId(id)
    }

    override fun findSlots(id: Long, limit: Int): List<Slot> {
        return slotRepository.findByDoctorId(id).take(limit)
    }
}