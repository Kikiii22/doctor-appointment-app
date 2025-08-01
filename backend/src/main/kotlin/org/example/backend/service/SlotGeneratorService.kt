package org.example.backend.service

import jakarta.annotation.PostConstruct
import org.example.backend.model.Doctor
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
@Component
@Order(2)
class SlotGeneratorService(
    private val doctorRepo: DoctorRepository,
    private val scheduleRepo: DoctorWorkingScheduleRepository,
    private val slotRepo: SlotRepository,
    private val doctorBreakRepo: DoctorBreakRepository,
    private val availableSlotRepo: AppointmentRepository
) {
    private val logger = LoggerFactory.getLogger(SlotGeneratorService::class.java)

    @PostConstruct
    fun backfillSlotsOnStartup() {
        val today = LocalDate.now()
        val oneMonthLater = today.plusMonths(1)
        generateSlotsForRange(today, oneMonthLater)
    }

    @Scheduled(cron = "0 0 0 * * ?")
    fun dailySlotGenerator() {
        val tomorrow = LocalDate.now().plusDays(1)
        generateSlotsForRange(tomorrow, tomorrow)
    }

    private fun generateSlotsForRange(from: LocalDate, to: LocalDate) {
        val doctors = doctorRepo.findAll()
        for (doctor in doctors) {
            var date = from
            while (!date.isAfter(to)) {
                try {
                    if (isDoctorOnBreak(doctor, date)) {
                        date = date.plusDays(1)
                        continue
                    }

                    val schedule = scheduleRepo.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                    if (schedule == null || !schedule.isWorking) {
                        date = date.plusDays(1)
                        continue
                    }

                    if (availableSlotRepo.existsByDoctorIdAndDate(doctor.id, date)) {
                        date = date.plusDays(1)
                        continue
                    }

                    generateAndSaveSlots(doctor, date, schedule)
                } catch (e: Exception) {
                    logger.error("Error for doctor ${doctor.id} on $date", e)
                }
                date = date.plusDays(1)
            }
        }
    }

    private fun isDoctorOnBreak(doctor: Doctor, date: LocalDate): Boolean {
        return doctorBreakRepo.existsByDoctorAndDate(doctor, date)
    }

    private fun generateAndSaveSlots(doctor: Doctor, date: LocalDate, schedule: DoctorWorkingSchedule) {
        val slotLength = Duration.ofMinutes(15)
        val start = date.atTime(schedule.startTime)
        val end = date.atTime(schedule.endTime)
        val breakStart = schedule.breakStart?.let { date.atTime(it) }
        val breakEnd = schedule.breakEnd?.let { date.atTime(it) }

        val slotsToSave = mutableListOf<Slot>()
        val availableSlots = mutableListOf<AvailableSlotForDoctor>()

        var current = start
        while (current.plus(slotLength) <= end) {
            val slotEnd = current.plus(slotLength)

            val inBreak = breakStart != null && breakEnd != null &&
                    (current < breakEnd && slotEnd > breakStart)

            if (!inBreak) {
                val slot = Slot(
                    date = date,
                    startTime = current.toLocalTime(),
                    endTime = slotEnd.toLocalTime()
                )
                slotsToSave.add(slot)
            }

            current = current.plus(slotLength)
        }

        val savedSlots = slotRepo.saveAll(slotsToSave)
        savedSlots.forEach { slot ->
            availableSlots.add(AvailableSlotForDoctor(slot = slot, doctor = doctor))
        }
        availableSlotRepo.saveAll(availableSlots)

        logger.info("Saved ${availableSlots.size} slots for doctor ${doctor.fullName} on $date")
    }
}
