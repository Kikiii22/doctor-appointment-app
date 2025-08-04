package org.example.backend.service

import org.example.backend.model.DoctorWorkingSchedule

interface DoctorService {
    fun findSchedule(id: Long): DoctorWorkingSchedule
}