package org.example.backend.model
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Id
import java.time.LocalDate
import java.time.LocalTime


@Entity
data class Slot(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    val date: LocalDate = LocalDate.now(),

    //all appointments will have a length of 15, if more is needed more appointments can be booked
    val startTime: LocalTime = LocalTime.of(9, 0),

    var booked: Boolean = false,

    @ManyToOne
    @JoinColumn(name="doctor_id")
    val doctor: Doctor
)
