package org.example.backend.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.dto.UserDto
import org.example.backend.model.Role
import org.example.backend.model.User
import org.example.backend.repository.UserRepository
import org.example.backend.service.TokenService
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class GoogleAuthenticationHandler (private val userRepository: UserRepository,
    private val tokenService: TokenService,private val jwtProperties: JwtProperties): AuthenticationSuccessHandler{
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oauthUser = authentication.principal as OAuth2User
        val email = oauthUser.getAttribute<String>("email") ?: throw RuntimeException("Email not found")

        // Create or get user
        val user = userRepository.findByEmail(email)
            ?: userRepository.save(User(username = email, email = email, role = Role.PATIENT))

        // Generate JWT
        val claims = mapOf("id" to user.id, "roles" to listOf(user.role.name))
        val jwt = tokenService.generate(
            org.example.backend.model.CustomUserDetails(user),
            Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration),
            claims
        )
        val userDto= UserDto(user.id,user.username,user.role,user.email)
        val userJson = URLEncoder.encode(ObjectMapper().writeValueAsString(userDto), StandardCharsets.UTF_8)
        response.sendRedirect("http://localhost:4200/login-success?token=$jwt&user=$userJson")
    }
    }

