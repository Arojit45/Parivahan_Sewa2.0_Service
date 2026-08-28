package com.parivahan.backend.vehicleregistration.controller;

import com.parivahan.backend.vehicleregistration.dto.*;
import com.parivahan.backend.vehicleregistration.service.VehicleRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vr")
@RequiredArgsConstructor
@Validated
public class VehicleRegistrationController {

    private final VehicleRegistrationService service;

    @PostMapping("/application")
    public ResponseEntity<VrApplicationResponse> createApplication(
            @Valid @RequestBody CreateVrRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createApplication(request));
    }

    @PutMapping("/application/{id}")
    public ResponseEntity<VrApplicationResponse> updateStep(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVrStepRequest request) {
        return ResponseEntity.ok(service.updateApplicationStep(id, request));
    }

    @GetMapping("/application/in-progress")
    public ResponseEntity<VrApplicationResponse> getInProgress() {
        return service.getInProgressApplication()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/application/mine")
    public ResponseEntity<List<VrApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(service.getMyApplications());
    }

    @PostMapping("/payment")
    public ResponseEntity<VrApplicationResponse> processPayment(
            @Valid @RequestBody MockVrPaymentRequest request) {
        return ResponseEntity.ok(service.processMockPayment(request));
    }

    @GetMapping("/track/{applicationNumber}")
    public ResponseEntity<TrackVrApplicationResponse> trackApplication(
            @PathVariable @NotBlank(message = "Application number is required") String applicationNumber) {
        return ResponseEntity.ok(service.trackApplication(applicationNumber));
    }

    @GetMapping("/fee")
    public ResponseEntity<Map<String, Object>> getFee(
            @RequestParam @NotBlank String vehicleType) {
        Map<String, java.math.BigDecimal> feeMap = Map.of(
                "Two Wheeler", new java.math.BigDecimal("300"),
                "Car", new java.math.BigDecimal("600"),
                "Transport Vehicle", new java.math.BigDecimal("1000")
        );
        java.math.BigDecimal fee = feeMap.getOrDefault(vehicleType, new java.math.BigDecimal("500"));
        return ResponseEntity.ok(Map.of("vehicleType", vehicleType, "fee", fee, "currency", "INR"));
    }
}
