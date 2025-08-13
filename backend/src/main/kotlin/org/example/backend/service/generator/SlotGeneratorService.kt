package org.example.backend.service.generator

import jakarta.annotation.PostConstruct
import org.example.backend.model.Doctor
import org.example.backend.model.DoctorBreak
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.model.Slot
import org.example.backend.repository.DoctorBreakRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.DoctorScheduleRepository
import org.example.backend.repository.SlotRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours


@Component
class SlotGeneratorService(
    private val doctorRepository: DoctorRepository,
    private val scheduleRepository: DoctorScheduleRepository,
    private val slotRepository: SlotRepository,
    private val breakRepository: DoctorBreakRepository
) {
    private val logger = LoggerFactory.getLogger(SlotGeneratorService::class.java)

    fun initializeNewDoctor(doctor: Doctor) {
        logger.info("Initializing schedule and slots for new doctor: {}", doctor.fullName)
        createDefaultScheduleForDoctor(doctor)
        generateSlotsForDoctor(doctor)
        logger.info("Completed initialization for doctor: {}", doctor.fullName)
    }

    private fun createDefaultScheduleForDoctor(doctor: Doctor) {
        val standardStart = LocalTime.of(9, 0)
        val standardEnd = LocalTime.of(17, 0)
        val breakStart = LocalTime.of(12, 0)
        val breakEnd = LocalTime.of(13, 0)

        val existingSchedules = scheduleRepository.findByDoctorId(doctor.id).map { it.dayOfWeek }.toSet()

        for (day in DayOfWeek.values().filter { it.value in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value }) {
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
                logger.debug("Created schedule for doctor {} on {}", doctor.fullName, day)
            }
        }
    }

    fun generateSlotsForDoctor(doctor: Doctor) {
        logger.info("Generating slots for doctor: {}", doctor.fullName)
        val from = LocalDate.now()
        val to = from.plusMonths(3)

        var date = from
        var totalSlotsGenerated = 0

        while (!date.isAfter(to)) {
            try {
                val isUnavailable = breakRepository.existsByDoctorIdAndDate(doctor.id, date)
                if (isUnavailable) {
                    logger.debug("Doctor {} unavailable on {}", doctor.fullName, date)
                    date = date.plusDays(1)
                    continue
                }

                val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                if (schedule == null || !schedule.isWorking) {
                    logger.debug("No working schedule for doctor {} on {}", doctor.fullName, date.dayOfWeek)
                    date = date.plusDays(1)
                    continue
                }

                if (slotRepository.existsByDoctorIdAndDate(doctor.id, date)) {
                    logger.debug("Slots already exist for doctor {} on {}", doctor.fullName, date)
                    date = date.plusDays(1)
                    continue
                }

                val slotsGenerated = generateSlotsForDoctorAndDate(doctor, date, schedule)
                totalSlotsGenerated += slotsGenerated

            } catch (e: Exception) {
                logger.error("Error generating slots for doctor ${doctor.fullName} on $date", e)
            }

            date = date.plusDays(1)
        }

        logger.info("Generated {} total slots for doctor {}", totalSlotsGenerated, doctor.fullName)
    }

    @PostConstruct
    fun initializeAllExistingDoctors() {
        logger.info("Initializing schedules and slots for all existing doctors...")
        val doctors = doctorRepository.findAll()

        if (doctors.isEmpty()) {
            logger.info("No doctors found in database")
            return
        }

        doctors.forEach { doctor ->
            try {
                initializeNewDoctor(doctor)
            } catch (e: Exception) {
                logger.error("Failed to initialize doctor: ${doctor.fullName}", e)
            }
        }

        logger.info("Completed initialization for {} doctors", doctors.size)
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    fun generateDailySlotsForAllDoctors() {
        logger.info("Running daily slot generation...")
        val tomorrow = LocalDate.now().plusDays(1)
        val endDate = tomorrow.plusMonths(3)

        val doctors = doctorRepository.findAll()
        if (doctors.isEmpty()) {
            logger.info("No doctors found for daily slot generation")
            return
        }

        doctors.forEach { doctor ->
            try {
                generateSlotsForDateRange(doctor, tomorrow, endDate)
            } catch (e: Exception) {
                logger.error("Failed daily slot generation for doctor: ${doctor.fullName}", e)
            }
        }

        logger.info("Completed daily slot generation")
    }

    private fun generateSlotsForDateRange(doctor: Doctor, fromDate: LocalDate, toDate: LocalDate) {
        var date = fromDate
        while (!date.isAfter(toDate)) {
            try {
                val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
                if (schedule == null || !schedule.isWorking) {
                    date = date.plusDays(1)
                    continue
                }

                if (slotRepository.existsByDoctorIdAndDate(doctor.id, date) ||
                    breakRepository.existsByDoctorIdAndDate(doctor.id, date)) {
                    date = date.plusDays(1)
                    continue
                }

                generateSlotsForDoctorAndDate(doctor, date, schedule)

            } catch (e: Exception) {
                logger.error("Error in generateSlotsForDateRange for doctor ${doctor.fullName} on $date", e)
            }
            date = date.plusDays(1)
        }
    }

    private fun generateSlotsForDoctorAndDate(
        doctor: Doctor,
        date: LocalDate,
        schedule: DoctorWorkingSchedule
    ): Int {
        val slotLength: Duration = 1.hours
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
}

//@Component
//class SlotGeneratorService(
//    private val doctorRepository: DoctorRepository,
//    private val scheduleRepository: DoctorScheduleRepository,
//    private val slotRepository: SlotRepository,
//    private val breakRepository: DoctorBreakRepository
//) {
//    @PostConstruct
//    fun createDefaultSchedulesForAllDoctors() {
//        val doctors = doctorRepository.findAll()
//
//        if (doctors.isEmpty()){
//            return
//        }
//        val standardStart = LocalTime.of(9, 0)
//        val standardEnd = LocalTime.of(17, 0)
//        val breakStart = LocalTime.of(12, 0)
//        val breakEnd = LocalTime.of(13, 0)
//        for (doctor in doctors) {
//            val existingSchedules = scheduleRepository.findByDoctorId(doctor.id).map { it.dayOfWeek }.toSet()
//
//            // Only MONDAY to FRIDAY
//            for (day in DayOfWeek.values().filter { it.value in DayOfWeek.MONDAY.value..DayOfWeek.FRIDAY.value }) {
//                if (day !in existingSchedules) {
//                    val schedule = DoctorWorkingSchedule(
//                        doctor = doctor,
//                        dayOfWeek = day,
//                        startTime = standardStart,
//                        endTime = standardEnd,
//                        breakStart = breakStart,
//                        breakEnd = breakEnd,
//                        isWorking = true
//                    )
//                    scheduleRepository.save(schedule)
//                }
//
//            }
//
//        }
//        val doctor1 = doctors.find { it.id == 1L }
//        if (!breakRepository.existsByDoctorIdAndDate(doctor1!!.id, LocalDate.of(2025, 8, 10))) {
//            val break1 = DoctorBreak(
//                doctor = doctor1,
//                dates = listOf(LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 11)),
//                reason = "Annual leave"
//            )
//            breakRepository.save(break1)
//        }
//
//
//    }
//
//    private val logger = LoggerFactory.getLogger(SlotGeneratorService::class.java)
//
//    @PostConstruct
//    fun backfillMissedSlots() {
//        logger.info("Application ready, generating slots...")
//        val today = LocalDate.now()
//        val future = today.plusMonths(3)
//        logger.info("Backfilling slots from $today to $future")
//        generateSlotsForDateRange(today, future)
//    }
//
//    @Scheduled(cron = "0 0 0 * * ?")
//    fun generateDoctorSlots() {
//        val from = LocalDate.now().plusDays(1)
//        val to = from.plusMonths(3)
//        val doctors = doctorRepository.findAll()
//
//        for (doctor in doctors) {
//            var date = from
//            while (!date.isAfter(to)) {
//                try {
//                    val isUnavailable = breakRepository.existsByDoctorIdAndDate(doctor.id, date)
//                    if (isUnavailable) {
//                        logger.debug("Doctor {} unavailable on {}", doctor.fullName, date)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
//                    if (schedule == null) {
//                        logger.debug("No working schedule for doctor {} on {}", doctor.fullName, date.dayOfWeek)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    if (slotRepository.existsByDoctorIdAndDate(doctor.id, date)) {
//                        logger.debug("Slots already exist for doctor {} on {}", doctor.fullName, date)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    generateSlotsForDoctorAndDate(doctor, date, schedule)
//
//                } catch (e: Exception) {
//                    logger.error("Error generating slots for doctor ${doctor.fullName} on $date", e)
//                }
//
//                date = date.plusDays(1)
//            }
//
//        }
//    }
//
//    fun generateDoctorSlots(doctor: Doctor) {
//        val from = LocalDate.now().plusDays(1)
//        val to = from.plusMonths(3)
//            var date = from
//            while (!date.isAfter(to)) {
//                try {
//                    val isUnavailable = breakRepository.existsByDoctorIdAndDate(doctor.id, date)
//                    if (isUnavailable) {
//                        logger.debug("Doctor {} unavailable on {}", doctor.fullName, date)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
//                    if (schedule == null) {
//                        logger.debug("No working schedule for doctor {} on {}", doctor.fullName, date.dayOfWeek)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    if (slotRepository.existsByDoctorIdAndDate(doctor.id, date)) {
//                        logger.debug("Slots already exist for doctor {} on {}", doctor.fullName, date)
//                        date = date.plusDays(1)
//                        continue
//                    }
//
//                    generateSlotsForDoctorAndDate(doctor, date, schedule)
//
//                } catch (e: Exception) {
//                    logger.error("Error generating slots for doctor ${doctor.fullName} on $date", e)
//                }
//
//                date = date.plusDays(1)
//            }
//    }
//
//    private fun generateSlotsForDoctorAndDate(
//        doctor: Doctor,
//        date: LocalDate,
//        schedule: DoctorWorkingSchedule
//    ): Int {
//        val slotLength: Duration = 1.hours
//        val start = date.atTime(schedule.startTime)
//        val end = date.atTime(schedule.endTime)
//
//        val breakStart = schedule.breakStart?.let { date.atTime(it) }
//        val breakEnd = schedule.breakEnd?.let { date.atTime(it) }
//
//        var current = start
//        var slotsGenerated = 0
//        val slotsToSave = mutableListOf<Slot>()
//
//        while (current.plusNanos(slotLength.inWholeNanoseconds) <= end) {
//            val slotEnd = current.plusNanos(slotLength.inWholeNanoseconds)
//
//            val inBreak = breakStart != null && breakEnd != null &&
//                    (current < breakEnd && slotEnd > breakStart)
//
//            if (!inBreak) {
//                val slot = Slot(
//                    doctor = doctor,
//                    date = date,
//                    startTime = current.toLocalTime(),
//                    booked = false
//                )
//                slotsToSave.add(slot)
//                slotsGenerated++
//            }
//
//            current = current.plusNanos(slotLength.inWholeNanoseconds)
//        }
//
//        if (slotsToSave.isNotEmpty()) {
//            slotRepository.saveAll(slotsToSave)
//            logger.debug("Saved {} slots for doctor {} on {}", slotsToSave.size, doctor.fullName, date)
//        }
//
//        return slotsGenerated
//    }
//
//    fun generateSlotsForDateRange(fromDate: LocalDate, toDate: LocalDate) {
//        logger.info("Generating slots from {} to {}", fromDate, toDate)
//        val doctors = doctorRepository.findAll()
//
//        for (doctor in doctors) {
//            var date = fromDate
//            while (!date.isAfter(toDate)) {
//                val schedule = scheduleRepository.findByDoctorIdAndDayOfWeek(doctor.id, date.dayOfWeek)
//                if (schedule == null) {
//                    logger.debug("No schedule for doctor ${doctor.fullName} on $date, skipping...")
//                    date = date.plusDays(1)
//                    continue
//                }
//                if (schedule != null && schedule.isWorking &&
//                    !slotRepository.existsByDoctorIdAndDate(doctor.id, date) &&
//                    !breakRepository.existsByDoctorIdAndDate(doctor.id, date)
//                ) {
//
//                    generateSlotsForDoctorAndDate(doctor, date, schedule)
//                }
//                date = date.plusDays(1)
//            }
//        }
//    }
//
//
//}