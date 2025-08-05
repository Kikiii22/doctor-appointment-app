package org.example.backend.service.impl

import org.example.backend.exception.DoctorNotFoundException
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.DoctorScheduleRepository
import org.example.backend.service.DoctorService
import org.springframework.stereotype.Service

@Service
class DoctorServiceImpl(
    private val doctorScheduleRepository: DoctorScheduleRepository,
    private val doctorRepository: DoctorRepository
) : DoctorService {
    override fun findSchedule(id: Long): List<DoctorWorkingSchedule> {
        return doctorScheduleRepository.findByDoctorId(id)
    }
}