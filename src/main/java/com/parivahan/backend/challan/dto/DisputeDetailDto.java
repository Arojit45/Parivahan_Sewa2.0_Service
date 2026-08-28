package com.parivahan.backend.challan.dto;

import com.parivahan.backend.challan.enums.DisputeReason;
import com.parivahan.backend.challan.enums.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DisputeDetailDto {
    private Long id;
    private String disputeNumber;
    private Long challanId;
    private String registrationNumber;
    private String offence;
    private DisputeReason reason;
    private String explanation;
    private String evidenceUrls;
    private DisputeStatus status;
    private String authorityResponse;
    private LocalDateTime submittedAt;
    private LocalDateTime resolvedAt;
    private List<DisputeTimelineEventDto> timeline;
}
