package org.example.backend.service.impl

import org.example.backend.model.Doctor
import org.example.backend.model.DoctorSlot
import org.example.backend.service.DoctorService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class DoctorServiceImpl(
    private val doctorRepo: DoctorRepository,
    private val hospitalRepo: HospitalRepository,
    private val scheduleRepo: DoctorWorkingScheduleRepository,
    private val breakRepo: DoctorBreakRepository,
    private val appointmentRepo: AppointmentRepository
) : DoctorService {
/**
    override fun searchDoctors(specialization: String?, name: String?, hospital: String?): List<Doctor> {
        return doctorRepo.findAll().filter {
            (specialization == null || it.specialization.contains(specialization, ignoreCase = true)) &&
                    (name == null || it.fullName.contains(name, ignoreCase = true)) &&
                    (hospital == null || it.hospital.name.contains(hospital, ignoreCase = true))
        }
    }
*/
    override fun getDoctorById(id: Long): Doctor {
        return doctorRepo.findById(id).orElseThrow { NoSuchElementException("Doctor not found") }
    }

    override fun getAllDoctors(): List<Doctor> {
        return doctorRepo.findAll()
    }

}
