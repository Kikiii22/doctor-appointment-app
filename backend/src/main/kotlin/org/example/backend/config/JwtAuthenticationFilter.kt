package org.example.backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.backend.service.CustomDetailsService
import org.example.backend.service.TokenService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: CustomDetailsService,
    private val tokenService: TokenService
) :
    OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        println("shouldNotFilter called for $path")
        return path.startsWith("/api/auth/") || path == "/api/roles"
                || path == "/api/hospitals"
                || path == "/api/departments"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("JWT Filter is running for: ${request.servletPath}")
        val headerAuth = request.getHeader("Authorization")
        val jwt = if (headerAuth?.startsWith("Bearer ") == true) headerAuth.substring(7) else null
        try {
            if (!jwt.isNullOrBlank() && !tokenService.isExpired(jwt)) {
                val claims = tokenService.getAllClaims(jwt)
                val username = tokenService.getUsername(jwt)
                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    val rolesFromToken = (claims["roles"] as? Collection<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val authorities = if (rolesFromToken.isNotEmpty()) {
                        rolesFromToken.map { SimpleGrantedAuthority("ROLE_$it") }
                    } else {
                        userDetails.authorities
                    }
                    if (tokenService.isValid(jwt, userDetails)) {
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                        )
                       val webDetails = WebAuthenticationDetailsSource().buildDetails(request)
                        val idFromToken = (claims["id"] as? Number)?.toLong()
                        authentication.details = mapOf(
                            "web" to webDetails,
                            "id" to idFromToken
                        )
                        println("JWT OK -> username=${username}, rolesFromToken=$rolesFromToken, authorities=$authorities, idFromToken=$idFromToken")
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
        } catch (e: Exception) {
            println("JWT authentication failed: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader("Authorization")
        return if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            headerAuth.substring(7)
        } else null
    }
}