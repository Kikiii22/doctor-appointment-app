package org.example.backend.service.impl

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.SlotRepository
import org.example.backend.service.AppointmentService
import org.springframework.stereotype.Service

@Service
class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val slotRepository: SlotRepository,
    private val patientRepository: PatientRepository
) : AppointmentService {

    override fun findAppointmentsByDoctor(id: Long): List<Appointment> {
        return appointmentRepository.findBySlotDoctorId(id)
    }

    override fun findAppointmentsByPatient(id: Long): List<Appointment> {
        return appointmentRepository.findByPatientId(id)
    }

    override fun findAppointmentsInHospital(id: Long): List<Appointment> {
        return appointmentRepository.findBySlotDoctorHospitalId(id)
    }

    override fun findAppointmentsByDepartment(id: Long): List<Appointment> {
        return appointmentRepository.findBySlotDoctorDepartmentId(id)
    }

    override fun bookAppointment(slotId: Long, patientId: Long): Appointment {
        val slot = slotRepository.findById(slotId).orElseThrow { RuntimeException("Slot not found") }
        if (slot.booked) throw RuntimeException("Slot already booked!")
        val patient = patientRepository.findById(patientId).orElseThrow { RuntimeException("Patient not found") }
        slot.booked = true
        slotRepository.save(slot)
        val appointment = Appointment(
            slot = slot,
            patient = patient,
            status = AppointmentStatus.BOOKED
        )
        return appointmentRepository.save(appointment)    }
}