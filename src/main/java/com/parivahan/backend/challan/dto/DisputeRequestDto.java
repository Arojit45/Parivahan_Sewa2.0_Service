package com.parivahan.backend.challan.dto;

import com.parivahan.backend.challan.enums.DisputeReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DisputeRequestDto {

    @NotNull(message = "Dispute reason is required")
    private DisputeReason reason;

    @NotBlank(message = "Explanation is required")
    @Size(min = 20, max = 1000, message = "Explanation must be between 20 and 1000 characters")
    private String explanation;

    // Comma-separated URLs of uploaded evidence files (optional)
    private String evidenceUrls;
}
