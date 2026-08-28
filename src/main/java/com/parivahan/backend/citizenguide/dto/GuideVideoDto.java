package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuideVideoDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String duration;
    private String thumbnailTone;
    private String thumbnailImage;
    private String icon;
    private String embedUrl;
}
