package org.example.backend.service

import org.example.backend.model.CustomUserDetails
import org.example.backend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomDetailsService(
    val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        println("Loading user by username: $username")

        val user = userRepository.findByUsername(username)
            ?: throw Exception("User not found")

        return CustomUserDetails(user)
    }
}