package org.example.backend.service

import org.example.backend.model.Doctor

interface DoctorService {
    fun getDoctorById(id: Long): Doctor
    fun getAllDoctors(): List<Doctor>
}
