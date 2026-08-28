package com.parivahan.backend.mydocuments.dto;

import com.parivahan.backend.drivinglicense.dto.ApplicationResponse;
import com.parivahan.backend.mydocuments.dto.MyVehicleDocumentDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyDocumentsResponse {
    private List<MyVehicleDocumentDto> vehicles;
    private ApplicationResponse drivingLicense;
}
