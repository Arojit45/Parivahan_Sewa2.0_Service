package com.parivahan.backend.assistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantAction {
    private String label;   // e.g. "Renew PUC"
    private String action;  // e.g. "PUC_RENEWAL" — controlled enum-style string
}
