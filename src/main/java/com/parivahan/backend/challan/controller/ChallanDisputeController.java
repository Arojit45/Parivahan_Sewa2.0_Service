package com.parivahan.backend.challan.controller;

import com.parivahan.backend.challan.dto.DisputeDetailDto;
import com.parivahan.backend.challan.dto.DisputeRequestDto;
import com.parivahan.backend.challan.dto.DisputeTimelineEventDto;
import com.parivahan.backend.challan.service.ChallanDisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChallanDisputeController {

    private final ChallanDisputeService disputeService;

    /** POST /api/v1/challans/{challanId}/dispute — Raise a dispute on a challan. */
    @PostMapping("/api/v1/challans/{challanId}/dispute")
    public ResponseEntity<DisputeDetailDto> raiseDispute(
            @PathVariable Long challanId,
            @Valid @RequestBody DisputeRequestDto request) {
        return new ResponseEntity<>(disputeService.raiseDispute(challanId, request), HttpStatus.CREATED);
    }

    /** GET /api/v1/challans/{challanId}/dispute — Get dispute details for a challan. */
    @GetMapping("/api/v1/challans/{challanId}/dispute")
    public ResponseEntity<DisputeDetailDto> getDisputeByChallan(@PathVariable Long challanId) {
        return ResponseEntity.ok(disputeService.getDisputeByChallanId(challanId));
    }

    /** GET /api/v1/disputes — All disputes for the current user. */
    @GetMapping("/api/v1/disputes")
    public ResponseEntity<List<DisputeDetailDto>> getAllMyDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputesForCurrentUser());
    }

    /** GET /api/v1/disputes/{disputeId}/timeline — Visual timeline for tracking. */
    @GetMapping("/api/v1/disputes/{disputeId}/timeline")
    public ResponseEntity<List<DisputeTimelineEventDto>> getDisputeTimeline(@PathVariable Long disputeId) {
        return ResponseEntity.ok(disputeService.getTimeline(disputeId));
    }

    /**
     * POST /api/v1/disputes/{disputeId}/authority-decision
     * Simulates the authority processing the dispute.
     * Future: this will be a webhook/callback from the government grievance portal.
     */
    @PostMapping("/api/v1/disputes/{disputeId}/authority-decision")
    public ResponseEntity<DisputeDetailDto> processAuthorityDecision(@PathVariable Long disputeId) {
        return ResponseEntity.ok(disputeService.processAuthorityDecision(disputeId));
    }
}
