package com.parivahan.backend.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertDto {

    public enum AlertType {
        CRITICAL, WARNING, INFO
    }

    private AlertType type;
    private String title;
    private String message;
}
