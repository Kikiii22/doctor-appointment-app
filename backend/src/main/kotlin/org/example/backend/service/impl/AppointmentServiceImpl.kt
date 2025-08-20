package org.example.backend.service.impl

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.SlotRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.AppointmentService
import org.example.backend.service.GoogleService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AppointmentServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val slotRepository: SlotRepository,
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
    private val googleService: GoogleService,
    private val authorizedClientService: OAuth2AuthorizedClientService
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
        val patient=patientRepository.findByUserId(patientId)?: throw RuntimeException("Patient not found")
        if (slot.booked) throw RuntimeException("Slot already booked!")
        slot.booked = true
        slotRepository.save(slot)
        val appointment = Appointment(
            slot = slot,
            patient = patient,
            status = AppointmentStatus.BOOKED
        )
        val savedAppointment = appointmentRepository.save(appointment)

        // --- Check if the patient is Google-authenticated ---
        val principal = SecurityContextHolder.getContext().authentication.principal

        if (principal is OAuth2User) {
            // Load User entity using email
            val email = principal.getAttribute<String>("email")
                ?: throw RuntimeException("Email not found in OAuth2User")
            val user = userRepository.findByEmail(email)
                ?: throw RuntimeException("User not found for email: $email")

            // If you have the OAuth2AuthorizedClientService, you can get the access token
            val auth = SecurityContextHolder.getContext().authentication
            if (auth is OAuth2AuthenticationToken) {
                val authorizedClient: org.springframework.security.oauth2.client.OAuth2AuthorizedClient? =
                    authorizedClientService.loadAuthorizedClient(auth.authorizedClientRegistrationId, auth.name)


                if (authorizedClient != null) {
                    val accessToken = authorizedClient.accessToken.tokenValue
                    googleService.addEvent(user, savedAppointment, accessToken)
                }
            }
        }

        return savedAppointment    }

    override fun cancelAppointment(slotId: Long, patientId: Long) {
        val slot = slotRepository.findById(slotId).orElseThrow { RuntimeException("Slot not found") }
        if (!slot.booked) throw RuntimeException("Slot not booked!")
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

        appointment.status = AppointmentStatus.FINISHED
        appointment.description = description
        return appointmentRepository.save(appointment)
    }


}