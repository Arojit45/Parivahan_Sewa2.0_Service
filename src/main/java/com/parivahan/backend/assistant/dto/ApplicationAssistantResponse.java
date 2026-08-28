package com.parivahan.backend.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationAssistantResponse {
    private String answer;
    private List<AssistantAction> actions;
    private List<String> sources;
    private boolean fallback;
}
