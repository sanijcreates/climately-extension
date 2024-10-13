package com.backend.hh24.controllers;

import com.backend.hh24.services.calendarService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class CalendarController {

    private final calendarService googleCalendarService;
    private Credential credential;

    public CalendarController(calendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @GetMapping("/")
    public String random() {
        System.out.println("working");
        return "working";
    }

    @GetMapping("/oAuth")
    public ResponseEntity<String> getAuth() {
        String authorizationUrl = googleCalendarService.getAuthorizationUrl();
        return ResponseEntity.ok(authorizationUrl);
    }

    @GetMapping("/Callback")
    public ResponseEntity<String> handleOAuthCallback(@RequestParam("code") String code) {
        try {
            this.credential = googleCalendarService.getCredentials(code);
            return ResponseEntity.ok("Authorization successful. You can now use the calendar services.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents() {
        if (credential == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please authorize first using /oAuth endpoint");
        }
        try {
            Calendar service = googleCalendarService.getCalendarService(credential);
            List<Event> events = googleCalendarService.listEvents(service);
            return ResponseEntity.ok(events);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/createEvent")
    public ResponseEntity<?> createEvent(@RequestParam String summary, @RequestParam String location, @RequestParam String description, @RequestParam String startDateTime, @RequestParam String endDateTime) {
        if (credential == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please authorize first using /oAuth endpoint");
        }
        try {
            Calendar service = googleCalendarService.getCalendarService(credential);
            Event event = googleCalendarService.createEvent(service, summary, location, description, startDateTime, endDateTime);
            return ResponseEntity.ok(event);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
