package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceGuideDto {
    private String id;
    private String title;
    private String summary;
    private String icon;
    private String accent;
    private List<String> steps;
    private String relatedVideoId;
}
