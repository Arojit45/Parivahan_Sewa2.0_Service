package com.parivahan.backend.correction.controller;

import com.parivahan.backend.correction.entity.CorrectionRequest;
import com.parivahan.backend.correction.repository.CorrectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/debug/corrections")
@RequiredArgsConstructor
public class DebugCorrectionController {
    private final CorrectionRepository repo;
    
    @GetMapping
    public List<CorrectionRequest> getAll() {
        return repo.findAll();
    }
}
