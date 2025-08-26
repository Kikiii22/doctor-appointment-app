package org.example.backend.service.embedding

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@Service
class EmbeddingService {

    private val restTemplate = RestTemplate()
    private val pythonUrl = "http://localhost:8000/embedding/similarity"

    fun findRelevantDepartments(input: String): List<Pair<String, Double>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val body = mapOf("sentence" to input)
        val request = HttpEntity(body, headers)

        val response = restTemplate.postForObject(pythonUrl, request, Map::class.java) as Map<String, Double>
        return response.entries.map { it.key to it.value }.sortedByDescending { it.second }
    }
}
