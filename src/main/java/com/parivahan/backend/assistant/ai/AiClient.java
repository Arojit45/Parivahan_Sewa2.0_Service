package com.parivahan.backend.assistant.ai;

/**
 * Abstraction over the underlying LLM provider.
 * The rest of the application depends on this interface, not on Gemini directly.
 * This makes it easy to swap providers in the future.
 */
public interface AiClient {

    /**
     * Generate a response from the AI model.
     *
     * @param systemPrompt instructions that define the assistant's behavior and constraints
     * @param context      trusted vehicle data prepared by VehicleContextBuilder (never raw DB records)
     * @param userQuestion the citizen's natural-language question
     * @return the AI-generated answer text
     */
    String generate(String systemPrompt, String context, String userQuestion);
}
