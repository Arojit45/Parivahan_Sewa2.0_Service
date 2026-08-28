package com.parivahan.backend.assistant.controller;

import com.parivahan.backend.assistant.dto.VehicleAssistantResponse;
import com.parivahan.backend.assistant.dto.VehicleQuestionRequest;
import com.parivahan.backend.assistant.service.VehicleAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Ask My Vehicle AI assistant.
 *
 * SECURITY:
 *  - No userId is accepted from the request body or query parameters.
 *  - The authenticated user's identity comes exclusively from the JWT (via Spring Security context).
 *  - Vehicle ownership is verified inside VehicleAssistantService before any data is fetched.
 */
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/assistant")
@RequiredArgsConstructor
public class VehicleAssistantController {

    private final VehicleAssistantService vehicleAssistantService;

    /**
     * POST /api/vehicles/{vehicleId}/assistant
     * Body: { "message": "Why is my health score low?" }
     *
     * Returns: { "answer": "...", "intent": "HEALTH_SCORE", "actions": [...], "sources": [...] }
     */
    @PostMapping
    public ResponseEntity<VehicleAssistantResponse> askVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleQuestionRequest request) {

        VehicleAssistantResponse response = vehicleAssistantService.askVehicle(vehicleId, request);
        return ResponseEntity.ok(response);
    }
}
