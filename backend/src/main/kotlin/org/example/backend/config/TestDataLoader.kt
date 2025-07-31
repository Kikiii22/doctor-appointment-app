package org.example.backend.config

import org.example.backend.model.DoctorBreak
import org.example.backend.model.Hospital
import org.example.backend.model.Role
import org.example.backend.repository.DoctorBreakRepository
import org.example.backend.repository.HospitalRepository
import java.time.LocalDate

// DataLoader.kt - Creates test data on startup

import org.example.backend.model.Doctor
import org.example.backend.model.DoctorWorkingSchedule
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.DoctorWorkingScheduleRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalTime

@Component
@Order(1) // Runs before SlotGeneratorService
class TestDataLoader(
    private val doctorRepo: DoctorRepository,
    private val scheduleRepo: DoctorWorkingScheduleRepository, private val hospitalRepo: HospitalRepository,
    private val unavailabilityRepo: DoctorBreakRepository // Add this

) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(TestDataLoader::class.java)

    override fun run(vararg args: String?) {
        if (doctorRepo.count() == 0L) {
            logger.info("Creating test data...")
            createTestData()
            logger.info("Test data created successfully")

        } else {
            logger.info("Test data already exists, skipping creation")
        }
    }

    private fun createTestData() {
        val doctor1 = Doctor(
            fullName = "Dr.John Smith",
            email = "john.smith@hospital.com",
            phone = "+1-555-0101",
            specialization = "Cardio",
            role = Role.DOCTOR,
        )

        val savedDoctor = doctorRepo.saveAndFlush(doctor1)
        logger.info("Created doctors: $savedDoctor.fullName}")

        // Create working schedules for Doctor 1
        val doctor1Schedules = listOf(
            // Monday to Friday: 9 AM - 5 PM with 1-2 PM break
            DoctorWorkingSchedule(
                doctor = savedDoctor,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                breakStart = LocalTime.of(13, 0),
                breakEnd = LocalTime.of(14, 0),
                isWorking = true
            ),
            DoctorWorkingSchedule(
                doctor = savedDoctor,
                dayOfWeek = DayOfWeek.TUESDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                breakStart = LocalTime.of(13, 0),
                breakEnd = LocalTime.of(14, 0),
                isWorking = true
            ),
            DoctorWorkingSchedule(
                doctor = savedDoctor,
                dayOfWeek = DayOfWeek.WEDNESDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                breakStart = LocalTime.of(13, 0),
                breakEnd = LocalTime.of(14, 0),
                isWorking = true
            ),
            DoctorWorkingSchedule(
                doctor = savedDoctor,
                dayOfWeek = DayOfWeek.THURSDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                breakStart = LocalTime.of(13, 0),
                breakEnd = LocalTime.of(14, 0),
                isWorking = true
            ),
            DoctorWorkingSchedule(
                doctor = savedDoctor,
                dayOfWeek = DayOfWeek.FRIDAY,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(17, 0),
                breakStart = LocalTime.of(13, 0),
                breakEnd = LocalTime.of(14, 0),
                isWorking = true
            ),

        )



        scheduleRepo.saveAll(doctor1Schedules)
        val unavailableDate = LocalDate.now().plusDays(3)
        unavailabilityRepo.save(
            DoctorBreak(
                doctor = savedDoctor,
                date = unavailableDate,
                reason = "Conference attendance"
            )
        )

        logger.info("Test doctor, schedule, and unavailability created.")
    }
}