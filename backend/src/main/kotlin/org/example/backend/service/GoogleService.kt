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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
class GoogleService {

    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    fun addEvent(user: User, appointment: Appointment, accessToken: String) {
        val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential().setAccessToken(accessToken)
        val service = Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
            .setApplicationName("doctorapp")
            .build()
        val zoneId = ZoneId.of("Europe/Skopje") // CET/CEST
        val startZoned: ZonedDateTime = appointment.slot.date.atTime(appointment.slot.startTime).atZone(zoneId)
        val endZoned: ZonedDateTime = startZoned.plusHours(1)

// Convert ZonedDateTime to java.util.Date
        val startDate = Date.from(startZoned.toInstant())
        val endDate = Date.from(endZoned.toInstant())

// Create Google DateTime
        val startDateTime = DateTime(startDate, java.util.TimeZone.getTimeZone(zoneId))
        val endDateTime = DateTime(endDate, java.util.TimeZone.getTimeZone(zoneId))

        val event = Event()
            .setSummary("Appointment with Dr. ${appointment.slot.doctor.fullName}")
            .setDescription("Appointment booked by ${user.username}")
            .setStart(EventDateTime().setDateTime(startDateTime).setTimeZone(zoneId.id))
            .setEnd(EventDateTime().setDateTime(endDateTime).setTimeZone(zoneId.id))

        service.events().insert("primary", event).execute()  }
}
