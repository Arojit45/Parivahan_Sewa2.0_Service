package com.parivahan.backend.correction.dto;

import com.parivahan.backend.correction.enums.CorrectionStatus;
import com.parivahan.backend.correction.enums.CorrectionTargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CorrectionResponse {
    private Long id;
    private CorrectionTargetType targetType;
    private Long targetId;
    private String fieldName;
    private String currentValue;
    private String requestedValue;
    private String reason;
    private CorrectionStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
