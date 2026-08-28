package com.parivahan.backend.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleQuestionRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    /** Optional UI language code for the assistant response. */
    @Pattern(
            regexp = "^(en|hi|bn|mr|ta|te|kn|ml|gu|pa|or)$",
            message = "Unsupported language"
    )
    private String language;

    /** Optional: conversation history for session context (list of prior exchanges). */
    private java.util.List<ConversationTurn> history;

    @Data
    public static class ConversationTurn {
        private String role;   // "user" or "assistant"
        private String content;
    }
}
