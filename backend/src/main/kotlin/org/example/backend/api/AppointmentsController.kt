package org.example.backend.api

import org.example.backend.dto.AppointmentRequest
import org.example.backend.dto.AuthUserDto
import org.example.backend.dto.FinishAppointmentRequest
import org.example.backend.model.Appointment
import org.example.backend.model.AppointmentStatus
import org.example.backend.repository.AppointmentRepository
import org.example.backend.repository.DoctorRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.AppointmentService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = ["http://localhost:4200"])
class AppointmentsController(
    private val appointmentService: AppointmentService,
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val userRepository: UserRepository
) {
    @PostMapping("/book")
    fun createAppointment(
        @RequestBody request: AppointmentRequest, @AuthenticationPrincipal principal: Any
    ): ResponseEntity<Appointment> {
        val userId = when (val principal = SecurityContextHolder.getContext().authentication.principal) {
            is AuthUserDto -> principal.id
            is OAuth2User -> {

                val email = principal.getAttribute<String>("email")
                    ?: throw RuntimeException("Email not found in OAuth2User")
                val user = userRepository.findByEmail(email)
                    ?: throw RuntimeException("User not found for email: $email")
                println("User is logged in via Google: ${principal.getAttribute<String>("email")}")

                user.id
            }
            else -> throw RuntimeException("Unknown principal type: ${principal::class.simpleName}")
        }

        return ResponseEntity.ok(appointmentService.bookAppointment(request.slotId, userId))

    }

    data class FinishAppointmentRequest(
        val doctorId: Long,
        val description: String
    )


    @PatchMapping("/{id}/finish")
    fun finishAppointment(
        @PathVariable id: Long,
        @RequestBody request: FinishAppointmentRequest
    ): ResponseEntity<Any> {
        val appointment = appointmentService.finishAppointment(id, request.doctorId, request.description)
        return ResponseEntity.ok(appointment)
    }



    @PostMapping("/cancel")
    fun cancelAppointment(
        @RequestBody request: AppointmentRequest,@AuthenticationPrincipal principal: Any
    ): ResponseEntity<Void> {
        val userId = when (val principal = SecurityContextHolder.getContext().authentication.principal) {
            is AuthUserDto -> principal.id
            is OAuth2User -> {

                val email = principal.getAttribute<String>("email")
                    ?: throw RuntimeException("Email not found in OAuth2User")
                val user = userRepository.findByEmail(email)
                    ?: throw RuntimeException("User not found for email: $email")
                println("User is logged in via Google: ${principal.getAttribute<String>("email")}")

                user.id
            }
            else -> throw RuntimeException("Unknown principal type: ${principal::class.simpleName}")
        }

        appointmentService.cancelAppointment(request.slotId, userId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/slot/{id}")
    fun getAppointmentBySlot(@PathVariable id: Long): ResponseEntity<Appointment>{
        return ResponseEntity.ok(appointmentRepository.findBySlotId(id))
    }
}
