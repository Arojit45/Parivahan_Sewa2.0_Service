package com.parivahan.backend.assistant.dto;

import com.parivahan.backend.assistant.model.VehicleIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAssistantResponse {
    private String answer;
    private VehicleIntent intent;
    private List<AssistantAction> actions;
    private List<String> sources;
    private boolean fallback; // true if Gemini was unavailable
}
