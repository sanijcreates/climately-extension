package com.backend.hh24.controllers;

import com.backend.hh24.services.calendarService;
import com.backend.hh24.services.weatherService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

//    private static class WeatherEvent {
//        LocalDateTime dateTime;
//        String condition;
//        double temperature;
//        double windSpeed;
//        double precipitation;
//
//        WeatherEvent(LocalDateTime dateTime, String condition, double temperature, double windSpeed, double precipitation) {
//            this.dateTime = dateTime;
//            this.condition = condition;
//            this.temperature = temperature;
//            this.windSpeed = windSpeed;
//            this.precipitation = precipitation;
//        }
//    }

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

            List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

            for (int i = 0; i < conditions.length; i++) {
                String condition = conditions[i].trim();
                double temperature = Double.parseDouble(temperatures[i].trim());
                double windSpeed = Double.parseDouble(windSpeeds[i].trim());
                double precipitation = Double.parseDouble(otherMetrics[i].trim());

                LocalDateTime eventDateTime = LocalDateTime.parse(date + " " + String.format("%02d:00", i), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                hourlyWeatherList.add(new HourlyWeather(eventDateTime, condition, temperature, windSpeed, precipitation));
            }

            List<WeatherEvent> mergedEvents = mergeWeatherEvents(hourlyWeatherList);
            weatherEvents.addAll(mergedEvents);

            for (WeatherEvent event : mergedEvents) {
                formattedForecast.append("[")
                        .append(event.startTime.toString())
                        .append("-")
                        .append(event.endTime.toString())
                        .append(", ")
                        .append(event.condition)
                        .append(", ")
                        .append(String.format("%.1fF", event.temperature * 9/5 + 32))
                        .append(", Windspeed/mph ")
                        .append(String.format("%.1f", event.windSpeed * 0.621371))
                        .append(", Precipitation ")
                        .append(String.format("%.1f", event.precipitation))
                        .append("], ");
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
        System.out.println(formattedForecast);

        return weatherEvents;
    }

    private List<WeatherEvent> mergeWeatherEvents(List<HourlyWeather> hourlyWeatherList) {
        List<WeatherEvent> mergedEvents = new ArrayList<>();
        List<HourlyWeather> currentMerge = new ArrayList<>();

        for (HourlyWeather hourly : hourlyWeatherList) {
            if (hourly.condition.equals("Clear") || hourly.condition.equals("Sunny")) {
                if (!currentMerge.isEmpty()) {
                    mergedEvents.add(createMergedEvent(currentMerge));
                    currentMerge.clear();
                }
            } else {
                currentMerge.add(hourly);
            }
        }

        if (!currentMerge.isEmpty()) {
            mergedEvents.add(createMergedEvent(currentMerge));
        }

        return mergedEvents;
    }

    private WeatherEvent createMergedEvent(List<HourlyWeather> hourlyWeathers) {
        LocalDateTime startTime = hourlyWeathers.get(0).dateTime;
        LocalDateTime endTime = hourlyWeathers.get(hourlyWeathers.size() - 1).dateTime.plusHours(1);
        String condition = getMostSevereCondition(hourlyWeathers);
        double avgTemperature = hourlyWeathers.stream().mapToDouble(hw -> hw.temperature).average().orElse(0);
        double avgWindSpeed = hourlyWeathers.stream().mapToDouble(hw -> hw.windSpeed).average().orElse(0);
        double avgPrecipitation = hourlyWeathers.stream().mapToDouble(hw -> hw.precipitation).average().orElse(0);

        return new WeatherEvent(startTime, endTime, condition, avgTemperature, avgWindSpeed, avgPrecipitation);
    }

    private String getMostSevereCondition(List<HourlyWeather> hourlyWeathers) {
        return hourlyWeathers.stream()
                .max(Comparator.comparingInt(hw -> getConditionSeverity(hw.condition)))
                .map(hw -> hw.condition)
                .orElse("Unknown");
    }

    private int getConditionSeverity(String condition) {
        Map<String, Integer> severityMap = new HashMap<>();
        severityMap.put("Light rain shower", 5);
        severityMap.put("Patchy light rain", 4);
        severityMap.put("Patchy rain nearby", 3);
        severityMap.put("Patchy light drizzle", 2);
        severityMap.put("Cloudy", 1);
        severityMap.put("Partly Cloudy", 1);
        severityMap.put("Overcast", 1);

        return severityMap.getOrDefault(condition, 0);
    }

    private static class HourlyWeather {
        LocalDateTime dateTime;
        String condition;
        double temperature;
        double windSpeed;
        double precipitation;

        HourlyWeather(LocalDateTime dateTime, String condition, double temperature, double windSpeed, double precipitation) {
            this.dateTime = dateTime;
            this.condition = condition;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.precipitation = precipitation;
        }
    }

    private static class WeatherEvent {
        LocalDateTime startTime;
        LocalDateTime endTime;
        String condition;
        double temperature;
        double windSpeed;
        double precipitation;

        WeatherEvent(LocalDateTime startTime, LocalDateTime endTime, String condition, double temperature, double windSpeed, double precipitation) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.condition = condition;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.precipitation = precipitation;
        }
    }

    private List<Event> createWeatherEvents(Calendar service, List<WeatherEvent> weatherEvents, String location) throws IOException {
        List<Event> createdEvents = new ArrayList<>();

        Map<String, String> conditionToEmoji = new HashMap<>();
        conditionToEmoji.put("Cloudy", "☁️");
        conditionToEmoji.put("Partly Cloudy", "⛅");
        conditionToEmoji.put("Overcast", "☁️");
        conditionToEmoji.put("Light rain shower", "🌦️");
        conditionToEmoji.put("Patchy light rain", "🌦️");
        conditionToEmoji.put("Patchy rain nearby", "🌦️");
        conditionToEmoji.put("Patchy light drizzle", "🌧️");
        conditionToEmoji.put("Heavy rain", "🌧️");
        conditionToEmoji.put("Thunderstorm", "⛈️");
        conditionToEmoji.put("Snow", "❄️");
        conditionToEmoji.put("Fog", "🌫️");


        for (WeatherEvent weatherEvent : weatherEvents) {
            String emoji = conditionToEmoji.getOrDefault(weatherEvent.condition, "🌡️");
            String summary = emoji + " " + weatherEvent.condition;

            String description = String.format("From %s to %s:\n" +
                            "Temperature: %.1f°C (%.1f°F)\n" +
                            "Wind Speed: %.1f km/h (%.1f mph)\n" +
                            "Precipitation: %.1f%%",
                    weatherEvent.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    weatherEvent.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    weatherEvent.temperature,
                    weatherEvent.temperature * 9/5 + 32,
                    weatherEvent.windSpeed,
                    weatherEvent.windSpeed * 0.621371,
                    weatherEvent.precipitation);


            Event event = new Event()
                    .setSummary(summary)
                    .setLocation(location)
                    .setDescription(description);

            DateTime startDateTime = new DateTime(weatherEvent.startTime.toInstant(ZoneOffset.UTC).toEpochMilli());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("UTC");
            event.setStart(start);


            DateTime endDateTime = new DateTime(weatherEvent.endTime.toInstant(ZoneOffset.UTC).toEpochMilli());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("UTC");
            event.setEnd(end);

            event.setColorId("5")  // Banana (light yellow)
                    .setTransparency("transparent")
                    .setVisibility("public");

            // Add a low-key background event
//            event.setEventType("outOfOffice");

            EventReminder[] reminderOverrides = new EventReminder[] {};
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            event = service.events().insert("primary", event).execute();
            createdEvents.add(event);
        }

        return createdEvents;
    }


}
