package com.backend.hh24.controllers;

import com.backend.hh24.services.calendarService;
import com.backend.hh24.services.weatherService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;



@RestController
public class CalendarController {
    private final WeatherController weatherController;
    private final calendarService googleCalendarService;
    private Credential credential;
    private StringBuilder formattedForecast;

    public CalendarController(calendarService googleCalendarService, WeatherController weatherController) {
        this.googleCalendarService = googleCalendarService;
        this.weatherController =weatherController;
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

    //create a final endpoint that runs the get events api, and /forecast api. print both the api results.
    @GetMapping("/finalEndpoint")
    public ResponseEntity<?> getFinalData(@RequestParam String location) {
        if (credential == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please authorize first using /oAuth endpoint");
        }

        StringBuilder result = new StringBuilder();

        try {
            // Get calendar events
            Calendar service = googleCalendarService.getCalendarService(credential);
            List<Event> events = googleCalendarService.listEvents(service);

            result.append("Calendar Events:\n");
            for (Event event : events) {
                result.append(event.getSummary()).append(" (").append(event.getStart().getDateTime()).append(")\n");
            }
            result.append("\n");

            // Get weather forecast
            String forecast = weatherController.getWeatherForecast(location);
            List<WeatherEvent> weatherEvents = parseWeatherForecast(forecast);
            List<Event> createdEvents = createWeatherEvents(service, weatherEvents, location);
            System.out.println(weatherEvents.toString());
            result.append("Weather Forecast:\n").append(forecast);

            return ResponseEntity.ok(result.toString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private static class WeatherEvent {
        LocalDateTime dateTime;
        String condition;
        double temperature;
        double windSpeed;
        double precipitation;

        WeatherEvent(LocalDateTime dateTime, String condition, double temperature, double windSpeed, double precipitation) {
            this.dateTime = dateTime;
            this.condition = condition;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.precipitation = precipitation;
        }
    }

    private List<WeatherEvent> parseWeatherForecast(String forecast) {
        List<WeatherEvent> weatherEvents = new ArrayList<>();
        formattedForecast = new StringBuilder("[[");
        String[] days = forecast.split("Date: ");

        for (int dayIndex = 1; dayIndex < days.length; dayIndex++) {
            String day = days[dayIndex];
            if (day.trim().isEmpty()) continue;

            String[] parts = day.split("Conditions: ");
            String date = parts[0].trim();
            formattedForecast.append("Date ").append(date).append(" : [");

            String[] conditions = parts[1].split("Temperatures:")[0].replace("[", "").replace("]", "").split(",");
            String[] temperatures = parts[1].split("Temperatures:")[1].split("Wind Speeds:")[0].replace("[", "").replace("]", "").split(",");
            String[] windSpeeds = parts[1].split("Wind Speeds:")[1].split("Other Metric:")[0].replace("[", "").replace("]", "").split(",");
            String[] otherMetrics = parts[1].split("Other Metric:")[1].replace("[", "").replace("]", "").split(",");

            for (int i = 0; i < conditions.length; i++) {
                String condition = conditions[i].trim();
                if (!condition.equals("Clear") && !condition.equals("Sunny")) {
                    double temperature = Double.parseDouble(temperatures[i].trim());
                    double windSpeed = Double.parseDouble(windSpeeds[i].trim());
                    double precipitation = Double.parseDouble(otherMetrics[i].trim());

                    LocalDateTime eventDateTime = LocalDateTime.parse(date + " " + String.format("%02d:00", i), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    formattedForecast.append("[")
                            .append(eventDateTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .append(", ")
                            .append(condition)
                            .append(", ")
                            .append(String.format("%.1fF", temperature * 9/5 + 32))
                            .append(", Windspeed/mph ")
                            .append(String.format("%.1f", windSpeed * 0.621371))
                            .append(", Precipitation ")
                            .append(String.format("%.1f", precipitation))
                            .append("], ");
                    weatherEvents.add(new WeatherEvent(eventDateTime, condition, temperature, windSpeed, precipitation));
                }
            }

            if (formattedForecast.charAt(formattedForecast.length() - 1) == ' ') {
                formattedForecast.setLength(formattedForecast.length() - 2);
            }

            formattedForecast.append("], ");
        }

        if (formattedForecast.charAt(formattedForecast.length() - 1) == ' ') {
            formattedForecast.setLength(formattedForecast.length() - 2);
        }

        formattedForecast.append("]]");
        System.out.println(formattedForecast.toString());

        return weatherEvents;
    }

    private List<Event> createWeatherEvents(Calendar service, List<WeatherEvent> weatherEvents,String location) throws IOException {
        List<Event> createdEvents = new ArrayList<>();

        for (WeatherEvent weatherEvent : weatherEvents) {
            String summary = weatherEvent.condition + " (" + weatherEvent.temperature + "°C)";
            String description = "Significant weather event: " + weatherEvent.condition + " with temperature " + weatherEvent.temperature + "°C";
            String startDateTime = weatherEvent.dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endDateTime = weatherEvent.dateTime.plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Event event = googleCalendarService.createEvent(service, summary, location, description, startDateTime, endDateTime);
            createdEvents.add(event);
        }

        return createdEvents;
    }

}
