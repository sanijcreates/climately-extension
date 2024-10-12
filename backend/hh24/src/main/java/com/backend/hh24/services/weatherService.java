package com.backend.hh24.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class weatherService {

    @Value("${weatherapi.key}")
    private String apiKey;

    private final String BASE_URL = "http://api.weatherapi.com/v1";

    private final RestTemplate restTemplate;

    public weatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getCurrentWeather(String location) {
        String url = BASE_URL + "/current.json?key=" + apiKey + "&q=" + location;
        return restTemplate.getForObject(url, String.class);
    }

    public String getForecastWeather(String location) {
        String url = BASE_URL + "/forecast.json?key=" + apiKey + "&q=" + location + "&days=4";
        return restTemplate.getForObject(url, String.class);
    }

    // Add more methods for other API endpoints as needed
}