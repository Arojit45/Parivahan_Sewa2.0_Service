package com.parivahan.backend.assistant.controller;

import com.parivahan.backend.assistant.dto.ApplicationAssistantResponse;
import com.parivahan.backend.assistant.dto.VehicleQuestionRequest;
import com.parivahan.backend.assistant.service.ApplicationAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/application-assistant")
@RequiredArgsConstructor
public class ApplicationAssistantController {

    private final ApplicationAssistantService applicationAssistantService;

    @PostMapping
    public ResponseEntity<ApplicationAssistantResponse> askApplicationProcess(
            @Valid @RequestBody VehicleQuestionRequest request) {
        return ResponseEntity.ok(applicationAssistantService.ask(request));
    }
}
