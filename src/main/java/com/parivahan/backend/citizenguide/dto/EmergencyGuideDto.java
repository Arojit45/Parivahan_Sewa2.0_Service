package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmergencyGuideDto {
    private String id;
    private String title;
    private String summary;
    private String icon;
}
