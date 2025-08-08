package org.example.backend.service.impl

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.SlotRepository
import org.example.backend.service.AppointmentService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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
        return appointmentRepository.save(appointment)
    }

    override fun cancelAppointment(slotId: Long, patientId: Long) {
        val slot = slotRepository.findById(slotId).orElseThrow { RuntimeException("Slot not found") }
        if (!slot.booked) throw RuntimeException("Slot not booked!")
        patientRepository.findById(patientId).orElseThrow { RuntimeException("Patient not found") }
        slot.booked = false
        slotRepository.save(slot)
        val appointment = appointmentRepository.findBySlot(slot)
        appointmentRepository.delete(appointment)
    }

    override fun finishAppointment(
        appointmentId: Long,
        doctorId: Long,
        description: String
    ): Appointment {
        val appointment =
            appointmentRepository.findById(appointmentId).orElseThrow { RuntimeException("Appointment not found") }
        if (appointment.status == AppointmentStatus.FINISHED) throw RuntimeException("Appointment already finished!")
        if (appointment.slot.doctor.id != doctorId) throw RuntimeException("Not your appointment!")
        val now = LocalDateTime.now()
        val startDateTime = LocalDateTime.of(appointment.slot.date, appointment.slot.startTime)

        if (now.isBefore(startDateTime.plusMinutes(15))) {
            throw IllegalStateException("You can only finish the appointment 15 minutes after its scheduled start time.")
        }

        appointment.status = AppointmentStatus.FINISHED
        appointment.description = description
        return appointmentRepository.save(appointment)
    }


}