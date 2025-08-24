package org.example.backend.repository

import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.model.Slot
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByPatientId(id: Long, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlotDoctorId(id: Long, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlotDoctorHospitalId(id: Long, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlotDoctorDepartmentId(id: Long, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlot(slot: Slot, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): Appointment
    fun findBySlotDateAndSlotStartTimeBeforeAndStatus(date: LocalDate, startTime: LocalTime, status: AppointmentStatus, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlotDoctorIdAndStatusAndDescription(
        id: Long,
        status: AppointmentStatus = AppointmentStatus.FINISHED,
        description: String = "",
        sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))
    ): List<Appointment>

    fun findBySlotDoctorIdAndStatus(id: Long, status: AppointmentStatus = AppointmentStatus.FINISHED, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findByPatientIdAndStatus(id: Long, status: AppointmentStatus = AppointmentStatus.FINISHED, sort: Sort = Sort.by("slot.date").and(Sort.by("slot.startTime"))): List<Appointment>
    fun findBySlotId(id: Long): Appointment
}