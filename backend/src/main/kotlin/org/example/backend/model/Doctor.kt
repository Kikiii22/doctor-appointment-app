package org.example.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "doctors")
data class Doctor(
    @Id @GeneratedValue
        (strategy = GenerationType.AUTO)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    val hospital: Hospital = Hospital(),

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    val department: Department = Department(),

    @Column(name = "full_name")
    val fullName: String = "",

    val email: String = "",

    val phone: String = "",

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User()
)
