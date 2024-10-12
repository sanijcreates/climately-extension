package com.backend.hh24.controllers;

import com.backend.hh24.services.calendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    @GetMapping("/events")
    public void getEvents() {

            googleCalendarService.listEvents();

    }
}
