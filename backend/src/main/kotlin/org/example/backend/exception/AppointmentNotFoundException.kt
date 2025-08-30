package org.example.backend.exception

class AppointmentNotFoundException(id: Long) :
    Exception("Appointment with id $id not found")