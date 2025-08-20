package org.example.backend.service

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import org.example.backend.model.Appointment
import org.example.backend.model.User
import org.springframework.stereotype.Service
import java.util.*

@Service
class GoogleService {

    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    fun addEvent(user: User, appointment: Appointment, accessToken: String) {
        val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential().setAccessToken(accessToken)
        val service = Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
            .setApplicationName("doctorapp")
            .build()
        val startDateTime = appointment.slot.date.atTime(appointment.slot.startTime)
        val endDateTime = startDateTime.plusHours(1)
        val event = Event()
            .setSummary("Appointment with Dr. ${appointment.slot.doctor.fullName}")
            .setDescription("Appointment booked by ${user.username}")
            .setStart(
                EventDateTime().setDateTime(DateTime(startDateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli())).setTimeZone("UTC")
            )
            .setEnd(
                EventDateTime().setDateTime(DateTime(endDateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli())).setTimeZone("UTC")
            )

        service.events().insert("primary", event).execute()
    }
}
