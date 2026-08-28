package com.parivahan.backend.vehicle.service;

import com.parivahan.backend.vehicle.dto.CaptchaResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    // Simple in-memory cache for demo purposes
    private final Map<String, Integer> captchaCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public CaptchaResponse generateCaptcha() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int answer = a + b;
        
        String captchaId = UUID.randomUUID().toString();
        String challenge = "What is " + a + " + " + b + "?";
        
        captchaCache.put(captchaId, answer);
        
        return CaptchaResponse.builder()
                .captchaId(captchaId)
                .challenge(challenge)
                .build();
    }

    public boolean validateCaptcha(String captchaId, String captchaAnswer) {
        if (captchaId == null || captchaAnswer == null) {
            return false;
        }
        
        Integer correctAnswer = captchaCache.get(captchaId);
        if (correctAnswer == null) {
            return false; // Expired or invalid
        }
        
        try {
            int providedAnswer = Integer.parseInt(captchaAnswer.trim());
            if (providedAnswer == correctAnswer) {
                captchaCache.remove(captchaId); // One-time use
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return false;
    }
}
