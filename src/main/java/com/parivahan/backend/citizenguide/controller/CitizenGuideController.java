package com.parivahan.backend.citizenguide.controller;

import com.parivahan.backend.citizenguide.dto.CitizenGuideResponse;
import com.parivahan.backend.citizenguide.dto.GuideDetailResponse;
import com.parivahan.backend.citizenguide.service.CitizenGuideService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citizen-guide")
@RequiredArgsConstructor
@Validated
public class CitizenGuideController {

    private final CitizenGuideService service;

    @GetMapping
    public ResponseEntity<CitizenGuideResponse> getGuide(
            @RequestParam(required = false)
            @Pattern(regexp = "^[a-zA-Z-]*$", message = "Category can contain only letters and hyphens")
            String category,
            @RequestParam(required = false)
            @Size(max = 80, message = "Search text cannot exceed 80 characters")
            String search) {
        return ResponseEntity.ok(service.getGuide(category, search));
    }

    @GetMapping("/guides/{guideId}")
    public ResponseEntity<GuideDetailResponse> getGuideDetail(
            @PathVariable
            @Pattern(regexp = "^[a-z0-9-]+$", message = "Guide id is invalid")
            String guideId) {
        return ResponseEntity.ok(service.getGuideDetail(guideId));
    }
}
