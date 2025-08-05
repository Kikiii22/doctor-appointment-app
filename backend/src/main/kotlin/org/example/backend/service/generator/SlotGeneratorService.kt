package org.example.backend.service.generator

import jakarta.annotation.PostConstruct
import org.example.backend.model.Doctor
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot
import org.example.backend.repository.DoctorBreakRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.DoctorScheduleRepository
import org.example.backend.repository.SlotRepository
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


@Component
@Order(2)
class SlotGeneratorService(
    private val doctorRepository: DoctorRepository,
    private val scheduleRepository: DoctorScheduleRepository,
    private val slotRepository: SlotRepository,
    private val breakRepository: DoctorBreakRepository
) {
    @PostConstruct
    fun createDefaultSchedulesForAllDoctors() {
        val doctors = doctorRepository.findAll()
        val standardStart = LocalTime.of(9, 0)
        val standardEnd = LocalTime.of(17, 0)
        val breakStart = LocalTime.of(12, 0)
        val breakEnd = LocalTime.of(13, 0)

        for (doctor in doctors) {
            val existingSchedules = scheduleRepository.findByDoctorId(doctor.id).map { it.dayOfWeek }.toSet()

            for (day in DayOfWeek.values()) {
                if (day !in existingSchedules) {
                    val schedule = DoctorWorkingSchedule(
                        doctor = doctor,
                        dayOfWeek = day,
                        startTime = standardStart,
                        endTime = standardEnd,
                        breakStart = breakStart,
                        breakEnd = breakEnd,
                        isWorking = true
                    )
                    scheduleRepository.save(schedule)
                }
            }
        }
    }

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
        val doctors = doctorRepository.findAll()

        for (doctor in doctors) {
            var date = from
            while (!date.isAfter(to)) {
                try {
                    val isUnavailable = breakRepository.existsByDoctorAndDate(doctor.id, date)
                    if (isUnavailable) {
                        logger.debug("Doctor {} unavailable on {}", doctor.fullName, date)
                        date = date.plusDays(1)
                        continue
                    }

                    val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                    if (!schedule.isWorking) {
                        logger.debug("No working schedule for doctor {} on {}", doctor.fullName, date.dayOfWeek)
                        date = date.plusDays(1)
                        continue
                    }

                    if (slotRepository.existsByDoctorIdAndDate(doctor.id, date)) {
                        logger.debug("Slots already exist for doctor {} on {}", doctor.fullName, date)
                        date = date.plusDays(1)
                        continue
                    }

                    generateSlotsForDoctorAndDate(doctor, date, schedule)

                } catch (e: Exception) {
                    logger.error("Error generating slots for doctor ${doctor.fullName} on $date", e)
                }

                date = date.plusDays(1)
            }

        }
    }

    private fun generateSlotsForDoctorAndDate(
        doctor: Doctor,
        date: LocalDate,
        schedule: DoctorWorkingSchedule
    ): Int {
        val slotLength: Duration = 15.minutes
        val start = date.atTime(schedule.startTime)
        val end = date.atTime(schedule.endTime)

        val breakStart = schedule.breakStart?.let { date.atTime(it) }
        val breakEnd = schedule.breakEnd?.let { date.atTime(it) }

        var current = start
        var slotsGenerated = 0
        val slotsToSave = mutableListOf<Slot>()

        while (current.plusNanos(slotLength.inWholeNanoseconds) <= end) {
            val slotEnd = current.plusNanos(slotLength.inWholeNanoseconds)

            val inBreak = breakStart != null && breakEnd != null &&
                    (current < breakEnd && slotEnd > breakStart)

            if (!inBreak) {
                val slot = Slot(
                    doctor = doctor,
                    date = date,
                    startTime = current.toLocalTime(),
                    booked = false
                )
                slotsToSave.add(slot)
                slotsGenerated++
            }

            current = current.plusNanos(slotLength.inWholeNanoseconds)
        }

        if (slotsToSave.isNotEmpty()) {
            slotRepository.saveAll(slotsToSave)
            logger.debug("Saved {} slots for doctor {} on {}", slotsToSave.size, doctor.fullName, date)
        }

        return slotsGenerated
    }

    fun generateSlotsForDateRange(fromDate: LocalDate, toDate: LocalDate) {
        logger.info("Generating slots from {} to {}", fromDate, toDate)
        val doctors = doctorRepository.findAll()

        for (doctor in doctors) {
            var date = fromDate
            while (!date.isAfter(toDate)) {
                val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                if (schedule != null && schedule.isWorking &&
                    !slotRepository.existsByDoctorIdAndDate(doctor.id, date) &&
                    !breakRepository.existsByDoctorAndDate(doctor.id, date)
                ) {

                    generateSlotsForDoctorAndDate(doctor, date, schedule)
                }
                date = date.plusDays(1)
            }
        }
    }


}