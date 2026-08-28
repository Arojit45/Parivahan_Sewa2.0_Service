package com.parivahan.backend.user.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String profilePhoto; // Base64 string
}
