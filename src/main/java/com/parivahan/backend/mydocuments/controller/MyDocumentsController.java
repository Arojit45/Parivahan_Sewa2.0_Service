package com.parivahan.backend.mydocuments.controller;

import com.parivahan.backend.mydocuments.dto.MyDocumentsResponse;
import com.parivahan.backend.mydocuments.service.MyDocumentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class MyDocumentsController {

    private final MyDocumentsService service;

    @GetMapping("/mine")
    public ResponseEntity<MyDocumentsResponse> getMyDocuments() {
        return ResponseEntity.ok(service.getMyDocuments());
    }
}
