package com.parivahan.backend.drivinglicense.controller;

import com.parivahan.backend.drivinglicense.dto.*;
import com.parivahan.backend.drivinglicense.service.DrivingLicenseService;
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
@RequestMapping("/api/v1/dl")
@RequiredArgsConstructor
@Validated
public class DrivingLicenseController {

    private final DrivingLicenseService service;

    /**
     * POST /api/v1/dl/application
     * Creates a new DRAFT application for the authenticated user.
     * Returns existing in-progress application if one already exists.
     */
    @PostMapping("/application")
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createApplication(request));
    }

    /**
     * PUT /api/v1/dl/application/{id}
     * Saves the data for a completed wizard step.
     */
    @PutMapping("/application/{id}")
    public ResponseEntity<ApplicationResponse> updateStep(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStepRequest request) {
        return ResponseEntity.ok(service.updateApplicationStep(id, request));
    }

    /**
     * GET /api/v1/dl/application/in-progress
     * Returns the user's current in-progress application (for wizard resume).
     * Returns 204 No Content if no in-progress application exists.
     */
    @GetMapping("/application/in-progress")
    public ResponseEntity<ApplicationResponse> getInProgress() {
        return service.getInProgressApplication()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * GET /api/v1/dl/application/mine
     * Returns all applications belonging to the authenticated user.
     */
    @GetMapping("/application/mine")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(service.getMyApplications());
    }

    /**
     * POST /api/v1/dl/payment
     * Processes the mock payment. On SUCCESS: generates application number, sets status to SUBMITTED.
     */
    @PostMapping("/payment")
    public ResponseEntity<ApplicationResponse> processPayment(
            @Valid @RequestBody MockPaymentRequest request) {
        return ResponseEntity.ok(service.processMockPayment(request));
    }

    /**
     * GET /api/v1/dl/track/{applicationNumber}
     * Public endpoint — no authentication required.
     * Returns the current status of a driving license application.
     */
    @GetMapping("/track/{applicationNumber}")
    public ResponseEntity<TrackApplicationResponse> trackApplication(
            @PathVariable @NotBlank(message = "Application number is required") String applicationNumber) {
        return ResponseEntity.ok(service.trackApplication(applicationNumber));
    }

    /**
     * GET /api/v1/dl/schools?state=Maharashtra&city=Pune
     * Searches for registered driving schools in a given state/city.
     */
    @GetMapping("/schools")
    public ResponseEntity<List<DrivingSchoolResponse>> searchSchools(
            @RequestParam @NotBlank(message = "State is required") String state,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(service.searchSchools(state, city));
    }

    /**
     * GET /api/v1/dl/fee?vehicleClass=LMV
     * Returns the application fee for a given vehicle class.
     */
    @GetMapping("/fee")
    public ResponseEntity<Map<String, Object>> getFee(
            @RequestParam @NotBlank String vehicleClass) {
        Map<String, java.math.BigDecimal> feeMap = Map.of(
                "LMV",   new java.math.BigDecimal("700"),
                "MCWG",  new java.math.BigDecimal("500"),
                "MCWOG", new java.math.BigDecimal("500"),
                "HMV",   new java.math.BigDecimal("1000"),
                "HPMV",  new java.math.BigDecimal("1000"),
                "TRANS", new java.math.BigDecimal("800"),
                "HTV",   new java.math.BigDecimal("1000")
        );
        java.math.BigDecimal fee = feeMap.getOrDefault(vehicleClass.toUpperCase(), new java.math.BigDecimal("700"));
        return ResponseEntity.ok(Map.of("vehicleClass", vehicleClass, "fee", fee, "currency", "INR"));
    }
}
