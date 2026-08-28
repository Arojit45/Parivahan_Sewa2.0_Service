package com.parivahan.backend.challan.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DisputeTimelineEventDto {

    public enum StepStatus {
        DONE, ACTIVE, PENDING
    }

    private int step;
    private String label;
    private StepStatus stepStatus;
    private LocalDateTime date;
    private String description;
}
