package org.example.backend.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class New(
    private val userDetailsService: UserDetailsService,
    private val googleAuthenticationHandler: GoogleAuthenticationHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        println(">>> SecurityConfig loaded and being used! <<<")
        http
            .csrf { it.disable() }
            .cors { }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }

            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/**", "/api/departments", "/api/roles","/oauth2/**", "/api/hospitals",
                        ).permitAll()
                    .requestMatchers("/api/patients/*/appointments","/api/appointments/book","/api/appointments/cancel","/api/doctors","/api/doctors/*").hasAnyRole("PATIENT", "ADMIN").anyRequest().authenticated()
            }.oauth2Login { auth->auth.successHandler ( googleAuthenticationHandler ) }
            .userDetailsService(userDetailsService)
            .formLogin { it.disable() }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager = authConfig.authenticationManager
}
