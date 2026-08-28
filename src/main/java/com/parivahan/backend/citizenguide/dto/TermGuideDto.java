package com.parivahan.backend.citizenguide.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TermGuideDto {
    private String term;
    private String meaning;
}
