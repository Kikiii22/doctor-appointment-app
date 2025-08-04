package org.example.backend.exception

class DoctorNotFoundException(id: Long) : Exception("Doctor with id $id not found")