package org.example.backend.api

import org.example.backend.model.Role
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/roles")
class RoleController {
    @GetMapping
    fun getAllRoles(): List<String> {
        return Role.values().map { it.name }
    }
}