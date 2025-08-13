package org.example.backend.service

import org.example.backend.config.JwtProperties
import org.example.backend.dto.AuthRequest
import org.example.backend.dto.JwtResponse
import org.example.backend.dto.RegisterRequest
import org.example.backend.dto.UserDto
import org.example.backend.model.Doctor
import org.example.backend.model.Hospital
import org.example.backend.model.Patient
import org.example.backend.model.Role
import org.example.backend.model.User
import org.example.backend.repository.*
import org.example.backend.service.generator.SlotGeneratorService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

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
    private val slotGeneratorService: SlotGeneratorService,
    private val departmentRepository: DepartmentRepository
) {
    fun login(authRequest: AuthRequest): JwtResponse {
        //authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
        //
        println("Authenticating: ${authRequest.username}")
        authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
        println("passed")
        val domainUser=userRepository.findByUsername(authRequest.username) ?: throw Exception("User ${authRequest.username} not found")
        val user = userDetailsService.loadUserByUsername(authRequest.username)
        val claims = mapOf(
            "id" to domainUser.id,
            "roles" to listOf(domainUser.role.name)
        )
        val userToken = tokenService.generate(
            userDetails = user,
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration),
            additionalClaims = claims
        )
        val userDto= UserDto(domainUser.id,domainUser.username,domainUser.role,domainUser.email)
        println("JWT claims at issue time = ${tokenService.getAllClaims(userToken)}")
        return JwtResponse(userToken,userDto)
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
                val hospital = req.hospitalId?.let {
                    hospitalRepository.findById(it).orElseThrow { Exception("Hospital not found") }
                }
                    ?: throw IllegalArgumentException("Hospital is required for doctor")
                val department = req.departmentId?.let {
                    departmentRepository.findById(it).orElseThrow { Exception("Department not found") }
                }
                    ?: throw IllegalArgumentException("Department is required for doctor")

                val doctor: Doctor = doctorRepository.save(
                    Doctor(
                        fullName = req.fullName,
                        phone = req.phone,
                        user = newUser,
                        hospital = hospital,
                        department = department
                    )
                )

                slotGeneratorService.generateDoctorSlots(doctor)

            }

            Role.ADMIN -> {
                hospitalRepository.save(
                    Hospital(
                        phone = req.phone,
                        user = newUser
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid role")
        }
        val userDetails = org.example.backend.model.CustomUserDetails(newUser)
        val claims = mapOf(
            "id" to newUser.id,
            "roles" to listOf(newUser.role.name)
        )
        val jwt = tokenService.generate(
            userDetails = userDetails,
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration),
            additionalClaims = claims
        )
        val userDto= UserDto(newUser.id,newUser.username,newUser.role,newUser.email)
        return JwtResponse(jwt,userDto)
    }

}