package com.parivahan.backend.drivinglicense.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UpdateApplicationStepRequest {

    @NotNull(message = "Step number is required")
    @Min(value = 1, message = "Step must be at least 1")
    @Max(value = 9, message = "Step must be at most 9")
    private Integer completedStep;

    // Step 1
    @Size(min = 2, max = 100)
    private String state;

    @Size(min = 2, max = 10)
    private String stateCode;

    // Step 2
    @Size(min = 2, max = 20)
    private String rtoCode;

    @Size(min = 2, max = 200)
    private String rtoName;

    // Step 3
    @Size(min = 2, max = 20)
    private String vehicleClass;

    // Step 4
    private Boolean hasLL;

    @Pattern(
        regexp = "^[A-Z]{2}[0-9]{2}[0-9]{11}$|^$",
        message = "Invalid Learner's Licence number format"
    )
    private String llNumber;

    // Step 5
    @Size(min = 2, max = 150, message = "Applicant name must be between 2 and 150 characters")
    private String applicantName;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;

    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String address;

    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be exactly 12 digits")
    private String aadharNumber;

    private Boolean isEligible;

    // Step 6
    private Boolean documentsConfirmed;
    private Long selectedDrivingSchoolId;

    // Step 7
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;

    @Size(min = 4, max = 20)
    private String appointmentSlot;
}
