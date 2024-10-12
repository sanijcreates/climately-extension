package com.backend.hh24.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.backend.hh24.services.weatherService;
import org.springframework.web.bind.annotation.RestController;





@RestController
public class WeatherController {

    private final weatherService wService;

    public WeatherController(weatherService weatherService) {
        this.wService = weatherService;
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam String location) {
        return wService.getCurrentWeather(location);
    }

    @GetMapping("/forecast")
    public String getWeatherForecast(@RequestParam String location) {
        String jsonResponse = wService.getForecastWeather(location);
        JSONObject jsonObject = new JSONObject(jsonResponse);

        JSONArray forecastArray =  jsonObject.getJSONObject("forecast").getJSONArray("forecastday");

        List<DateTime> dateTimes = new ArrayList<>();

        // Loop through forecast days
        for (int i = 0; i < forecastArray.length(); i++) {
            JSONObject forecastDay = forecastArray.getJSONObject(i);
            String date = forecastDay.getString("date");

            // Get hourly data
            JSONArray hourlyArray = forecastDay.getJSONArray("hour");

            // Arrays to store hourly data (24 hours)
            String[] conditions = new String[24];
            double[] temps = new double[24];
            double[] windSpeeds = new double[24];
            double[] otherMetric = new double[24];  // e.g., humidity

            // Loop through each hour and extract necessary data
            for (int j = 0; j < hourlyArray.length(); j++) {
                JSONObject hourData = hourlyArray.getJSONObject(j);

                conditions[j] = hourData.getJSONObject("condition").getString("text");
                temps[j] = hourData.getDouble("temp_c");
                windSpeeds[j] = hourData.getDouble("wind_kph");
                otherMetric[j] = hourData.getDouble("humidity");  // Example: storing humidity
            }

            // Create DateTime object with the parsed data
            DateTime dateTime = new DateTime(date, conditions, temps, windSpeeds, otherMetric);
            dateTimes.add(dateTime);
        }

        // Output or return the parsed data (for demonstration, you could log it or return as a response)
        // Here, we just return the string representation of the DateTime objects
        return dateTimes.stream()
                .map(DateTime::toString)
                .collect(Collectors.joining("\n\n"));
    }
}