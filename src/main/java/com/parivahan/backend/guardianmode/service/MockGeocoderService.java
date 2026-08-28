package com.parivahan.backend.guardianmode.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Resolves a human-readable area name to geographic coordinates.
 *
 * Current implementation: hardcoded map of major Indian locations.
 * Future integration point: replace body of resolve() with a Google Maps / MapMyIndia
 * Geocoding API call.
 */
@Service
@Slf4j
public class MockGeocoderService {

    private static final Map<String, double[]> KNOWN_LOCATIONS = Map.ofEntries(
            Map.entry("connaught place", new double[]{28.6315, 77.2167}),
            Map.entry("connaught place delhi", new double[]{28.6315, 77.2167}),
            Map.entry("mg road pune", new double[]{18.5262, 73.8731}),
            Map.entry("mg road", new double[]{18.5262, 73.8731}),
            Map.entry("bandra mumbai", new double[]{19.0596, 72.8295}),
            Map.entry("koregaon park pune", new double[]{18.5362, 73.8939}),
            Map.entry("indiranagar bangalore", new double[]{12.9784, 77.6408}),
            Map.entry("indiranagar bengaluru", new double[]{12.9784, 77.6408}),
            Map.entry("anna nagar chennai", new double[]{13.0854, 80.2101}),
            Map.entry("salt lake kolkata", new double[]{22.5839, 88.4004}),
            Map.entry("hitech city hyderabad", new double[]{17.4435, 78.3772}),
            Map.entry("sector 18 noida", new double[]{28.5706, 77.3210}),
            Map.entry("gomti nagar lucknow", new double[]{26.8650, 80.9934}),
            Map.entry("c scheme jaipur", new double[]{26.9054, 75.8023})
    );

    /**
     * @param areaName user-provided area name
     * @return [latitude, longitude] or null if not found
     */
    public double[] resolve(String areaName) {
        if (areaName == null || areaName.isBlank()) return null;
        String normalized = areaName.toLowerCase().trim();
        double[] coords = KNOWN_LOCATIONS.get(normalized);
        if (coords == null) {
            log.warn("Geocoder: area '{}' not found in mock map. Future: call Google Maps API.", areaName);
        }
        return coords;
    }
}
