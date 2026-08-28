package com.parivahan.backend.vehicle.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaResponse {
    private String captchaId;
    private String challenge;
}
