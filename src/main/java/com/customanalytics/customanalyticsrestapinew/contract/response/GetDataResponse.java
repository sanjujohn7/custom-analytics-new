package com.customanalytics.customanalyticsrestapinew.contract.response;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class GetDataResponse {
    private List<Map<String, Object>> data;
    private Long sum;
    private Long count;
}
