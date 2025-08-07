package org.example.backend.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class New( private val userDetailsService: UserDetailsService,
                     jwtAuthenticationFilter: JwtAuthenticationFilter ) {


    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity,
                            jwtAuthenticationFilter: JwtAuthenticationFilter): SecurityFilterChain {
        println(">>> SecurityConfig loaded and being used! <<<")
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/**","api/departments","api/roles","api/hospitals").permitAll()
                    .anyRequest().authenticated()
            }
            .userDetailsService(userDetailsService) // <- This wires your custom service
            .formLogin { it.disable() }
            .addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager = authConfig.authenticationManager
}
