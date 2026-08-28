package com.parivahan.backend.drivinglicense.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DrivingSchoolResponse {
    private Long id;
    private String name;
    private String address;
    private String state;
    private String city;
    private String pincode;
    private String phone;
    private BigDecimal rating;
    private Boolean isGovernmentApproved;
    private String licenseNumber;
}
