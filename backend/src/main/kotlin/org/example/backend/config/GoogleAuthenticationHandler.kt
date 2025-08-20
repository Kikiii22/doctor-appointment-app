package org.example.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.dto.AuthUserDto
import org.example.backend.dto.UserDto
import org.example.backend.model.Patient
import org.example.backend.model.Role
import org.example.backend.model.User
import org.example.backend.repository.PatientRepository
import org.example.backend.repository.UserRepository
import org.example.backend.service.TokenService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class GoogleAuthenticationHandler (private val userRepository: UserRepository,
                                   private val tokenService: TokenService,private val jwtProperties: JwtProperties,private val patientRepository: PatientRepository): AuthenticationSuccessHandler{
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauthToken = authentication as? OAuth2AuthenticationToken
            ?: throw RuntimeException("Not a Google OAuth2 login")

        val oauthUser = oauthToken.principal as OAuth2User
        val email = oauthUser.getAttribute<String>("email")
            ?: throw RuntimeException("Email not found")
        val user = userRepository.findByEmail(email)
            ?: userRepository.save(User(username = email, email = email, role = Role.PATIENT))

        val fullName: String = oauthUser.getAttribute<String>("name") ?: ""
        val phone: String = oauthUser.getAttribute<String>("phoneNumber")?:" "// requires extra scope
if (patientRepository.findByUserId(user.id) == null) {
    val patient = patientRepository.save(
        Patient(
            fullName = fullName,
            phone = phone,
            user = user
        )
    )
}
        val authorities = listOf(SimpleGrantedAuthority("ROLE_PATIENT"))
        val newAuth = OAuth2AuthenticationToken(
            oauthUser,
            authorities,
            oauthToken.authorizedClientRegistrationId
        )
        SecurityContextHolder.getContext().authentication = newAuth

        println("Google Auth Success -> username=${user.username}, role=${user.role.name}, authorities=$authorities, id=${user.id}")

        // Generate JWT for frontend
        val claims = mapOf("id" to user.id, "roles" to listOf(user.role.name))
        val jwt = tokenService.generate(
            userDetails = org.example.backend.model.CustomUserDetails(user),
            expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration),
            additionalClaims = claims
        )

        val userJson = URLEncoder.encode(
            ObjectMapper().writeValueAsString(UserDto(user.id, user.username, user.role, user.email)),
            StandardCharsets.UTF_8
        )

        response.sendRedirect("http://localhost:4200/google-success?token=$jwt&user=$userJson")
    }

}
