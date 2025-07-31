package org.example.backend.service

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.example.backend.model.Doctor
import org.example.backend.model.DoctorSlot
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.repository.DoctorBreakRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.DoctorSlotRepository
import org.example.backend.repository.DoctorWorkingScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate

@Component
@Order(2)
class SlotGeneratorService(
    private val doctorRepo: DoctorRepository,
    private val scheduleRepo: DoctorWorkingScheduleRepository,
    private val slotRepo: DoctorSlotRepository,
    private val unavailabilityRepo: DoctorBreakRepository // ✅ new repo
){
    private val logger = LoggerFactory.getLogger(SlotGeneratorService::class.java)
    @PostConstruct
    fun backfillMissedSlots() {
        logger.info("Application ready, generating slots...")
        val today = LocalDate.now()
        val future = today.plusMonths(3)
        logger.info("Backfilling slots from $today to $future")
        generateSlotsForDateRange(today, future)
    }
    @Scheduled(cron = "0 0 0 * * ?")
    fun generateDoctorSlots() {
        val from = LocalDate.now().plusDays(1)
        val to = from.plusMonths(3)
        val doctors = doctorRepo.findAll()

        for (doctor in doctors) {
            var date = from
            while (!date.isAfter(to)) {
                try {
                    // Check if doctor is unavailable on this date
                    val isUnavailable = unavailabilityRepo.existsByDoctorAndDate(doctor, date)
                    if (isUnavailable) {
                        logger.debug("Doctor ${doctor.fullName} unavailable on $date")
                        date = date.plusDays(1)
                        continue
                    }

                    // Get working schedule for this day
                    val schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                    if (schedule == null || !schedule.isWorking) {
                        logger.debug("No working schedule for doctor ${doctor.fullName} on ${date.dayOfWeek}")
                        date = date.plusDays(1)
                        continue
                    }

                    // Skip if slots already exist for this doctor and date
                    if (slotRepo.existsByDoctorIdAndDate(doctor.id, date)) {
                        logger.debug("Slots already exist for doctor ${doctor.fullName} on $date")
                        date = date.plusDays(1)
                        continue
                    }

                    // Generate slots for this day
                    val slotsForDay = generateSlotsForDoctorAndDate(doctor, date, schedule)

                } catch (e: Exception) {
                    logger.error("Error generating slots for doctor ${doctor.fullName} on $date", e)
                }

                date = date.plusDays(1)
            }

        }
    }
    private fun generateSlotsForDoctorAndDate(
            doctor: Doctor, // Replace with your Doctor entity type
            date: LocalDate,
            schedule: DoctorWorkingSchedule // Replace with your DoctorWorkingSchedule entity type
        ): Int {
            val slotLength = Duration.ofMinutes(30)
            val start = date.atTime(schedule.startTime)
            val end = date.atTime(schedule.endTime)

            val breakStart = schedule.breakStart?.let { date.atTime(it) }
            val breakEnd = schedule.breakEnd?.let { date.atTime(it) }

            var current = start
            var slotsGenerated = 0
            val slotsToSave = mutableListOf<DoctorSlot>()

            while (current.plus(slotLength) <= end) {
                val slotEnd = current.plus(slotLength)

                // Check if slot overlaps with break time
                val inBreak = breakStart != null && breakEnd != null &&
                        (current < breakEnd && slotEnd > breakStart)

                if (!inBreak) {
                    val slot = DoctorSlot(
                        doctor = doctor,
                        date = date,
                        startTime = current.toLocalTime(),
                        endTime = slotEnd.toLocalTime(),
                        isBooked = false
                    )
                    slotsToSave.add(slot)
                    slotsGenerated++
                }

                current = current.plus(slotLength)
            }

            // Batch save for better performance
            if (slotsToSave.isNotEmpty()) {
                slotRepo.saveAll(slotsToSave)
                logger.debug("Saved ${slotsToSave.size} slots for doctor ${doctor.fullName} on $date")
            }

            return slotsGenerated
        }

        // For testing purposes - generates slots for a specific date range
        fun generateSlotsForDateRange(fromDate: LocalDate, toDate: LocalDate) {
            logger.info("Generating slots from $fromDate to $toDate")
            val doctors = doctorRepo.findAll()

            for (doctor in doctors) {
                var date = fromDate
                while (!date.isAfter(toDate)) {
                    val schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                    if (schedule != null && schedule.isWorking &&
                        !slotRepo.existsByDoctorIdAndDate(doctor.id, date) &&
                        !unavailabilityRepo.existsByDoctorAndDate(doctor, date)) {

                        generateSlotsForDoctorAndDate(doctor, date, schedule)
                    }
                    date = date.plusDays(1)
                }
            }
        }




}
