package com.customanalytics.customanalyticsrestapinew.contract;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class getDataResponse {
    private List<Map<String, Object>> data;
    private double sum;
    private Long count;
}
