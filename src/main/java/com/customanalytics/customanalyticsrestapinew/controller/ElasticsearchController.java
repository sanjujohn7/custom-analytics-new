package com.customanalytics.customanalyticsrestapinew.controller;

import com.customanalytics.customanalyticsrestapinew.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class ElasticsearchController {
    private final ElasticsearchService elasticsearchService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String indexName, @RequestParam MultipartFile file) {
        try {
            elasticsearchService.uploadFile(indexName, file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }
}
