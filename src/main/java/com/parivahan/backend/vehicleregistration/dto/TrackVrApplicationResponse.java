package com.parivahan.backend.vehicleregistration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackVrApplicationResponse {
    private String applicationNumber;
    private String applicantName; // Usually would come from user profile, mocked here or joined
    private String vehicleCategory;
    private String vehicleType;
    private String status;
    private String inspectionStatus;
}
