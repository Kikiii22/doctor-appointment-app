package org.example.backend.service

import org.example.backend.config.JwtProperties
import org.example.backend.dto.AuthRequest
import org.example.backend.dto.JwtResponse
import org.example.backend.dto.RegisterRequest
import org.example.backend.model.Doctor
import org.example.backend.model.Patient
import org.example.backend.model.Role
import org.example.backend.model.User
import org.example.backend.repository.DepartmentRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.HospitalRepository
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthService
    (
    private val authManager: AuthenticationManager,
    private val userDetailsService: CustomDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
    private val userRepository: UserRepository,
    private val patientRepository: PatientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val doctorRepository: DoctorRepository,
    private val hospitalRepository: HospitalRepository,
            private val departmentRepository: DepartmentRepository
) {
    fun login(authRequest: AuthRequest): JwtResponse {
        //authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
        //
        println("Authenticating: ${authRequest.username}")
        authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
        println("passed")
        val user = userDetailsService.loadUserByUsername(authRequest.username)
        val userToken = tokenService.generate(
            userDetails = user,
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration)
        )
        return JwtResponse(userToken)
    }

    fun register(req: RegisterRequest): JwtResponse {
        if (userRepository.existsByUsername(req.username)) {
            throw Exception("Username ${req.username} is already taken!")
        }
        if (userRepository.existsByEmail(req.email)) {
            throw Exception("Email ${req.email} is already taken!")
        }
        val newUser = userRepository.save(
            User(
                username = req.username,
                password = passwordEncoder.encode(req.password),
                role = req.role,
                email = req.email
            )
        )
        when (req.role) {
            Role.PATIENT -> {
                patientRepository.save(
                    Patient(
                        fullName = req.fullName,
                        phone = req.phone,
                        user = newUser
                    )
                )
            }
            Role.DOCTOR -> {
                val hospital = req.hospitalId?.let { hospitalRepository.findById(it).orElseThrow { Exception("Hospital not found") } }
                    ?: throw IllegalArgumentException("Hospital is required for doctor")
                val department = req.departmentId?.let { departmentRepository.findById(it).orElseThrow { Exception("Department not found") } }
                    ?: throw IllegalArgumentException("Department is required for doctor")

                doctorRepository.save(
                    Doctor(
                        fullName = req.fullName,
                        phone = req.phone,
                        user = newUser,
                        hospital = hospital,
                        department = department
                    )
                )
            }
            else -> throw IllegalArgumentException("Invalid role")
        }
        val userDetails = org.example.backend.model.CustomUserDetails(newUser)
        val jwt = tokenService.generate(
            userDetails = userDetails,
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration)
        )
        return JwtResponse(jwt)
    }

}