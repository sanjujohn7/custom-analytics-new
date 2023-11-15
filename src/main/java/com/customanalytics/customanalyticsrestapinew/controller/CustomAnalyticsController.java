package com.customanalytics.customanalyticsrestapinew.controller;

import com.customanalytics.customanalyticsrestapinew.contract.response.GetDataResponse;
import com.customanalytics.customanalyticsrestapinew.service.CustomAnalyticsService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/custom-analytics")
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

    @GetMapping("/search")
    public ResponseEntity<List<?>> search(
            @RequestParam String indexName,
            @RequestParam(required = false) String filterField,
            @RequestParam(required = false) String filterValue,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) int from,
            @RequestParam(required = false) int size
    ) {
        try {
            List<?> searchResults = customAnalyticsService.searchBasedOnFilterAndSort(indexName, filterField, filterValue, sortField, sortOrder, from, size);
            return ResponseEntity.ok(searchResults);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Collections.singletonList("Error while filtering data " + e.getMessage()));
        }

    }

    @GetMapping("/data")
    public ResponseEntity<GetDataResponse> getDataBetweenDatesAndCategory(
            @RequestParam String indexName,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String productCategory
    ) {
        {
            try {
                GetDataResponse searchResults = customAnalyticsService.getDataBetweenDatesAndCategory(indexName, fromDate, toDate, productCategory);
                return ResponseEntity.ok(searchResults);
            } catch (IOException e) {
                return ResponseEntity.status(500).body((GetDataResponse) Collections.singletonList("Error while fetching data " + e.getMessage()));
            }
        }
    }
}