package org.example.backend.service.impl

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.example.backend.repository.DoctorSlotRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.AppointmentService
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class AppointmentServiceImpl(
    private val userRepo: UserRepository,
    private val slotRepo: DoctorSlotRepository,
    private val appointmentRepo: AppointmentRepository
) : AppointmentService {

    @Transactional
    @Retryable(
        value = [OptimisticLockingFailureException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    override fun bookAppointment(userId: Long, slotId: Long): Appointment {
        val user = userRepo.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        try {
            val slot = slotRepo.findById(slotId)
                .orElseThrow { IllegalArgumentException("Slot not found") }

            if (slot.isBooked) {
                throw IllegalStateException("Slot already booked")
            }

            // Mark as booked and flush to trigger version check immediately
            slot.isBooked = true
            val savedSlot = slotRepo.saveAndFlush(slot)

            val appointment = Appointment(
                user = user,
                doctor = savedSlot.doctor,
                slot = savedSlot
            )

            return appointmentRepo.save(appointment)

        } catch (ex: OptimisticLockingFailureException) {
            // This will trigger the retry mechanism
            throw IllegalStateException("Slot was just booked by another user. Retrying...", ex)
        }
    }

    override fun getAppointmentsForUser(userId: Long): List<Appointment> {
        return appointmentRepo.findByUserId(userId)
    }

    @Transactional
    override fun markAppointmentFinished(appointmentId: Long, treatmentNote: String): Appointment {
        val appt = appointmentRepo.findById(appointmentId)
            .orElseThrow { NoSuchElementException("Appointment not found") }

        appt.status = AppointmentStatus.FINISHED
        appt.treatmentNote = treatmentNote

        return appointmentRepo.save(appt)
    }
}