package com.backend.hh24.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class calendarService {

    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String REDIRECT_URI = "http://localhost:8080/Callback";

    private final NetHttpTransport HTTP_TRANSPORT;
    private GoogleAuthorizationCodeFlow flow;
    private GoogleClientSecrets clientSecrets;

    public calendarService() throws IOException, GeneralSecurityException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        initializeFlow();
    }

    private void initializeFlow() throws IOException {
        InputStream in = calendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .build();
    }

    public Credential getCredentials(String authorizationCode) throws IOException {
        GoogleTokenResponse tokenResponse =
                new GoogleAuthorizationCodeTokenRequest(
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret(),
                        authorizationCode,
                        REDIRECT_URI)
                        .execute();

        return flow.createAndStoreCredential(tokenResponse, "user");
    }

    public Calendar getCalendarService(Credential credential) throws IOException {
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Event> listEvents(Calendar service) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events:");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            }
        }
        return items;
    }

    public Event createEvent(Calendar service, String summary, String location, String description, String startDateTime, String endDateTime) throws IOException {
        Event event = new Event()
                .setSummary(summary)
                .setLocation(location)
                .setDescription(description);

        DateTime startDate = new DateTime(startDateTime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDate)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDate = new DateTime(endDateTime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDate)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        String calendarId = "primary";
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        return event;
    }

    public List<Event> createBatchEvents(Calendar service, List<String> summaries, List<String> locations, List<String> descriptions, List<String> startDateTimes, List<String> endDateTimes) throws IOException {
        List<Event> createdEvents = new ArrayList<>();
        for (int i = 0; i < summaries.size(); i++) {
            Event event = createEvent(service, summaries.get(i), locations.get(i), descriptions.get(i), startDateTimes.get(i), endDateTimes.get(i));
            createdEvents.add(event);
        }
        return createdEvents;
    }
}