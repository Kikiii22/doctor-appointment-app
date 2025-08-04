package org.example.backend.exception

class HospitalNotFoundException(id: Long): Exception("Hospital with id $id not found") {
}