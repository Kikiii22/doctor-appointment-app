package org.example.backend.service

import org.example.backend.model.Doctor
import org.example.backend.model.DoctorSlot
import java.time.LocalDate
import java.util.UUID

interface DoctorService {
   // fun searchDoctors(specialization: String?, name: String?, hospital: String?): List<Doctor>
    fun getDoctorById(id: Long): Doctor
    fun getAllDoctors(): List<Doctor>
}
