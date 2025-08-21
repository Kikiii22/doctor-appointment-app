package org.example.backend.api

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.dto.AuthRequest
import org.example.backend.dto.JwtResponse
import org.example.backend.dto.RegisterRequest
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.AuthService
import org.example.backend.service.TokenService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
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
    fun login(@RequestBody req: AuthRequest): ResponseEntity<Any> {
        return try {
            val jwtResponse = authService.login(req)
            ResponseEntity.ok(jwtResponse)
        } catch (ex: UsernameNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to ex.message))
        } catch (ex: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("message" to ex.message))
        }
    }
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null) {
            SecurityContextHolder.clearContext()
            request.session.invalidate()
        }
        response.status = HttpServletResponse.SC_OK
    }

}
