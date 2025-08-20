package org.example.backend.service.scheduled

import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AppointmentStatusUpdater(
    private val appointmentRepository: AppointmentRepository
) {
    private val logger = LoggerFactory.getLogger(AppointmentStatusUpdater::class.java)

    @Scheduled(fixedRate = 300000)
    fun updateFinishedAppointments() {
        val now = LocalDateTime.now()
        val appointments = appointmentRepository.
        findBySlotDateAndSlotStartTimeBeforeAndStatus(
            now.toLocalDate(),
            now.toLocalTime(),
            AppointmentStatus.BOOKED)

        if (appointments.isEmpty()) {
            return
        }

        appointments.forEach {
            it.status = AppointmentStatus.FINISHED
        }

        appointmentRepository.saveAll(appointments)
        logger.info("Marked {} appointments as FINISHED", appointments.size)
    }
}
