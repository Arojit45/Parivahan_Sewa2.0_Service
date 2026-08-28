package com.parivahan.backend.ping;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * PingController — lightweight keep-alive endpoint.
 *
 * Called by the frontend every 60 seconds to prevent Render's free-tier
 * server from spinning down after 5 minutes of inactivity.
 *
 * Endpoint: GET /api/ping   (public — no JWT required)
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString(),
                "service", "Parivahan Sewa 2.0"
        ));
    }
}
