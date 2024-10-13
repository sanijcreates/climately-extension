package com.backend.hh24.controllers;

import com.backend.hh24.services.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    private final OpenAiService openAiService;

    @Autowired
    public OpenAiController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/completion")
    public String getCompletion(@RequestBody String prompt) {
            return openAiService.getCompletion(prompt);
    }
}
