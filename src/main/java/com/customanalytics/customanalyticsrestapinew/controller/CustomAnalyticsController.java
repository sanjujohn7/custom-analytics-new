package com.customanalytics.customanalyticsrestapinew.controller;

import com.customanalytics.customanalyticsrestapinew.service.CustomAnalyticsService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class CustomAnalyticsController {
    private final CustomAnalyticsService customAnalyticsService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam String indexName, @RequestParam MultipartFile file) {
        try {
            customAnalyticsService.uploadFile(indexName, file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<List<?>> getDataByIndexName(@RequestParam String indexName) {
        try {
            List<?> response = customAnalyticsService.getDataByIndexName(indexName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(
                            Collections.singletonList(
                                    "Error while retrieving data " + e.getMessage()));
        }
    }
}
