package org.example.backend.service

import org.example.backend.config.JwtProperties
import org.example.backend.dto.AuthRequest
import org.example.backend.dto.JwtResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthService
    (private val authManager: AuthenticationManager,
    private val userDetailsService: CustomDetailsService,
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties
){
    fun login(authRequest: AuthRequest): JwtResponse {


        //authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
       //
        println("Authenticating: ${authRequest.username}")
        authManager.authenticate(UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password))
        println("passed")
        val user=userDetailsService.loadUserByUsername(authRequest.username)
        val userToken=tokenService.generate(userDetails = user, expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration.expiration))
        return JwtResponse(userToken)
    }
}