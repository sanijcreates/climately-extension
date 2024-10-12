package com.backend.hh24.controllers;


import com.backend.hh24.services.calendarService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

}
