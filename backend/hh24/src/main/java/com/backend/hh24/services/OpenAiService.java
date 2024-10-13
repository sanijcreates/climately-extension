package com.backend.hh24.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.system.prompt}")
private String systemPrompt;

private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getCompletion(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        String requestBody = createRequestBody(prompt);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String createRequestBody(String prompt) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", "gpt-4");
            rootNode.put("max_tokens", 1000);

            ArrayNode messagesArray = rootNode.putArray("messages");

            ObjectNode systemMessageNode = messagesArray.addObject();
            systemMessageNode.put("role", "system");
            systemMessageNode.put("content", systemPrompt);

            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", "user");
            messageNode.put("content", prompt);

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Error creating request body", e);
        }
    }
}