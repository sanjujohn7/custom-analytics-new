package com.customanalytics.customanalyticsrestapinew.contract.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetDataResponse {
        private List<Map<String, Object>> data;
        private Long sum;
        private Long count;
    }

