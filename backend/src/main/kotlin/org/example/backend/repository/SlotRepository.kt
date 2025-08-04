package org.example.backend.repository

import org.example.backend.model.Doctor
import org.example.backend.model.Slot
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface SlotRepository : JpaRepository<Slot, Long> {
    fun findByDateAndDoctor(date: LocalDate, doctor: Doctor)
    fun findByDate(date: LocalDate)
    fun findByDoctor(doctor: Doctor)
    fun findByBooked(booked: Boolean)
}