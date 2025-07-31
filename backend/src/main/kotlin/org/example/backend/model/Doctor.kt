package org.example.backend.model
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.EnumType
import jakarta.persistence.GenerationType

import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name="doctor")
data class Doctor(
    @Id @GeneratedValue
        (strategy = GenerationType.AUTO)
    val id: Long = 0,
    val fullName: String="",

    val email: String="",

    val phone: String="",

    val specialization: String="",
/**
   // @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "hospital_id", nullable = false)
   // val hospital: Hospital,
**/
    @Enumerated(EnumType.STRING)
    val role: Role = Role.DOCTOR,
    //val password: String
)
