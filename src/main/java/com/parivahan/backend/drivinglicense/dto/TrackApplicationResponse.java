package com.parivahan.backend.drivinglicense.dto;

import com.parivahan.backend.drivinglicense.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TrackApplicationResponse {
    private String applicationNumber;
    private String applicantName;
    private String state;
    private String rtoName;
    private String vehicleClass;
    private ApplicationStatus applicationStatus;
    private String testResult;
    private LocalDate appointmentDate;
    private String appointmentSlot;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String statusMessage;
}
