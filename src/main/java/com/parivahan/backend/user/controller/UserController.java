package com.parivahan.backend.user.controller;

import com.parivahan.backend.common.exception.ResourceNotFoundException;
import com.parivahan.backend.user.domain.User;
import com.parivahan.backend.user.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import com.parivahan.backend.user.dto.ProfileUpdateRequest;
import com.parivahan.backend.user.dto.AuthResponse;

/**
 * User profile/preferences controller.
 * All endpoints identify the user exclusively from the JWT — no userId from request body.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** Supported language codes. */
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
            "en", "hi", "bn", "mr", "ta", "te", "kn", "ml", "gu", "pa", "or"
    );

    /**
     * PUT /api/user/language
     * Body: { "language": "hi" }
     * Saves the authenticated user's preferred language.
     * The language persists so future sessions use it by default.
     */
    @PutMapping("/language")
    public ResponseEntity<Void> updateLanguage(@RequestBody LanguageRequest request) {
        if (request.getLanguage() == null || !SUPPORTED_LANGUAGES.contains(request.getLanguage())) {
            return ResponseEntity.badRequest().build();
        }
        User user = getCurrentUser();
        user.setPreferredLanguage(request.getLanguage());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/user/language
     * Returns the current user's preferred language code.
     */
    @GetMapping("/language")
    public ResponseEntity<LanguageResponse> getLanguage() {
        User user = getCurrentUser();
        String lang = user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "en";
        return ResponseEntity.ok(new LanguageResponse(lang));
    }

    /**
     * PUT /api/user/profile
     * Body: ProfileUpdateRequest
     * Updates the user's profile information.
     */
    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(@RequestBody ProfileUpdateRequest request) {
        User user = getCurrentUser();
        
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getProfilePhoto() != null) {
            user.setProfilePhoto(request.getProfilePhoto());
        }
        
        userRepository.save(user);
        
        return ResponseEntity.ok(AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .profilePhoto(user.getProfilePhoto())
                .build());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = principal instanceof UserDetails ud ? ud.getUsername() : principal.toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Data
    public static class LanguageRequest {
        private String language;
    }

    @Data
    public static class LanguageResponse {
        private final String language;
    }
}
