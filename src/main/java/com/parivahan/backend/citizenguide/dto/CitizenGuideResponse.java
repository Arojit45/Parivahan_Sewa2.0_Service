package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CitizenGuideResponse {
    private List<GuideVideoDto> videos;
    private List<ServiceGuideDto> serviceGuides;
    private List<RuleGuideDto> rules;
    private List<String> dos;
    private List<String> donts;
    private List<EmergencyGuideDto> emergencyGuides;
    private List<TermGuideDto> terms;
}
