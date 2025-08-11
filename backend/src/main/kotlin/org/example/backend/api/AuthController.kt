package org.example.backend.api

import org.example.backend.dto.AuthRequest
import org.example.backend.dto.JwtResponse
import org.example.backend.dto.RegisterRequest
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.AuthService
import org.example.backend.service.TokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["http://localhost:4200"])
class AuthController(
    private val userRepository: UserRepository,
    private val patientRepository: PatientRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authService: AuthService,
    private val tokenService: TokenService
) {


    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): JwtResponse {
        return authService.register(req)
    }

    @PostMapping("/login")
    fun login(@RequestBody req: AuthRequest): JwtResponse {
        return authService.login(req)
    }
}
