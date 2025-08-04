package org.example.backend.service.impl

import org.example.backend.model.Appointment
import org.example.backend.repository.AppointmentRepository
import org.example.backend.service.AppointmentService
import org.springframework.stereotype.Service

@Service
class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository
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
}