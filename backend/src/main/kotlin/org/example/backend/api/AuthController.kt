package org.example.backend.api

import org.example.backend.dto.AuthRequest
import org.example.backend.dto.RegisterRequest
import org.example.backend.model.Patient
import org.example.backend.model.Role
import org.example.backend.model.User
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val patientRepository: PatientRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken")
        }

        val newUser = userRepository.save(
            User(
                username = req.username,
                password = passwordEncoder.encode(req.password),
                role = Role.PATIENT,
                email = req.email
            )
        )

        val newPatient = patientRepository.save(
            Patient(
                fullName = req.fullName,
                phone = req.phone,
                user = newUser
            )
        )

        return ResponseEntity.ok(newPatient)
    }

    @PostMapping("/login")
    fun login(@RequestBody req: AuthRequest): ResponseEntity<Any> {
        val user = userRepository.findByUsername(req.username)

        if (!passwordEncoder.matches(req.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials")
        }

        return ResponseEntity.ok(user)
    }
}
