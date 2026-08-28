package com.parivahan.backend.correction.controller;

import com.parivahan.backend.correction.dto.CorrectionResponse;
import com.parivahan.backend.correction.dto.SubmitCorrectionRequest;
import com.parivahan.backend.correction.service.CorrectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/corrections")
@RequiredArgsConstructor
@Validated
public class CorrectionController {

    private final CorrectionService service;

    @PostMapping
    public ResponseEntity<CorrectionResponse> submitCorrection(@Valid @RequestBody SubmitCorrectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitCorrection(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<CorrectionResponse>> getMyCorrections() {
        return ResponseEntity.ok(service.getMyCorrections());
    }

    // Admin endpoint to simulate authority approval
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<CorrectionResponse> approveCorrection(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveCorrection(id));
    }
}
