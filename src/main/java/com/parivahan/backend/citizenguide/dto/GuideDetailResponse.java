package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuideDetailResponse {
    private ServiceGuideDto guide;
    private GuideVideoDto video;
    private ServiceGuideDto nextGuide;
    private ServiceGuideDto previousGuide;
}
