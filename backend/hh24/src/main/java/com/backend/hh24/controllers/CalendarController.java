package com.backend.hh24.controllers;


import com.backend.hh24.services.calendarService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
public class CalendarController {

    private final calendarService googleCalendarService;

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
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            // Instead of calling listEvents directly, return the authorization URL
            String authorizationUrl = googleCalendarService.getAuthorizationUrl(HTTP_TRANSPORT);
            //how to kill the process after getting the authorization
            return ResponseEntity.ok(authorizationUrl);
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/events")
    public void getEvents() {
        googleCalendarService.listEvents();
    }

    @GetMapping("/Callback")
    public String handleOAuthCallback(@RequestParam("code") String code) {
        // Process the authorization code and exchange it for an access token
        System.out.println(code);
        return code;
    }

    @PostMapping("/createEvent")
    public ResponseEntity<String> createEvent(@RequestParam String summary, @RequestParam String location, @RequestParam String description, @RequestParam String startDateTime, @RequestParam String endDateTime) {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            googleCalendarService.createEvent(HTTP_TRANSPORT, summary, location, description, startDateTime, endDateTime);
            return ResponseEntity.ok("Event created successfully");
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/createBatchEvents")
    public ResponseEntity<String> createBatchEvents(@RequestParam List<String> summaries, @RequestParam List<String> locations, @RequestParam List<String> descriptions, @RequestParam List<String> startDateTimes, @RequestParam List<String> endDateTimes) {
        if (summaries.size() != locations.size() || summaries.size() != descriptions.size() || summaries.size() != startDateTimes.size() || summaries.size() != endDateTimes.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: All input lists must have the same size.");
        }
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            for (int i = 0; i < summaries.size(); i++) {
                googleCalendarService.createEvent(HTTP_TRANSPORT, summaries.get(i), locations.get(i), descriptions.get(i), startDateTimes.get(i), endDateTimes.get(i));
            }
            return ResponseEntity.ok("Batch events created successfully");
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
