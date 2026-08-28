package com.parivahan.backend.correction.dto;

import com.parivahan.backend.correction.enums.CorrectionTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitCorrectionRequest {
    @NotNull(message = "Target type is required")
    private CorrectionTargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotBlank(message = "Field name is required")
    private String fieldName;

    private String currentValue;

    @NotBlank(message = "Requested value is required")
    private String requestedValue;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String evidenceBase64;
}
