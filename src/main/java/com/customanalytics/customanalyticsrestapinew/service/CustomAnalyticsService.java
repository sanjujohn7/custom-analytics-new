package com.customanalytics.customanalyticsrestapinew.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.customanalytics.customanalyticsrestapinew.contract.response.GetDataResponse;
import com.customanalytics.customanalyticsrestapinew.exception.IndexAlreadyExistException;
import com.customanalytics.customanalyticsrestapinew.exception.IndexNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomAnalyticsService {

    private final RestHighLevelClient client;

    public void uploadFile(String indexName, MultipartFile file) throws IOException{
        Map<String, Object> resultMap = new HashMap<>();
        if (indexExists(indexName)) {
            throw new IndexAlreadyExistException("Index name already Exist");
        }
        try (CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(file.getInputStream())))) {
            String[] headers = csvReader.readNext(); // Assuming CSV uses comma as delimiter for columns
            if (headers != null) {
                List<String[]> rows = csvReader.readAll();
                List<Class<?>> columnDataTypes = inferColumnDataTypes(headers, rows);
                for (String[] data : rows) {
                    Map<String, Object> dataMap = createDataMap(headers, data, columnDataTypes);
                    IndexRequest indexRequest = new IndexRequest(indexName).source(dataMap);
                    IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);

                    if (response.getResult() != null) {
                        resultMap.put("success", "Data indexed successfully");
                    } else {
                        resultMap.put("error", "Failed to index data into Elasticsearch");
                    }
                }
            }
        } catch (Exception e) {
            resultMap.put("error", "Error processing the file or indexing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private List<Class<?>> inferColumnDataTypes(String[] headers, List<String[]> rows) {
        List<Class<?>> columnDataTypes = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            Class<?> dataType = inferDataType(headers[i], getColumnData(i, rows));
            columnDataTypes.add(dataType);
        }
        return columnDataTypes;
    }
    private List<String> getColumnData(int columnIndex, List<String[]> rows) {
        return rows.stream().map(row -> row[columnIndex]).collect(Collectors.toList());
    }
    private Class<?> inferDataType(String header, List<String> columnData) {
        boolean isNumeric = columnData.stream().allMatch(this::isNumeric);
        return isNumeric ? Double.class : String.class;
    }
    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private Map<String, Object> createDataMap(String[] headers, String[] data, List<Class<?>> columnDataTypes) {
        Map<String, Object> dataMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String formattedKey = headers[i].replaceAll("\\s+", "");
            String value = data[i];
            Class<?> dataType = columnDataTypes.get(i);
            if (dataType.equals(Double.class)) {
                try {
                    double numericValue = Double.parseDouble(value);
                    dataMap.put(formattedKey, numericValue);
                } catch (NumberFormatException e) {
                    dataMap.put(formattedKey, value);
                }
            } else {
                dataMap.put(formattedKey, value);
            }
        }
        return dataMap;
    }

    private boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    public List<Map<String, Object>> getDataByIndexName(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

        if (exists) {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            return extractDocuments(searchResponse);
        } else {
            throw new IndexNotFoundException("Index not found");
        }
    }

    private List<Map<String, Object>> extractDocuments(SearchResponse searchResponse) {
        List<Map<String, Object>> documents = new ArrayList<>();

        Arrays.stream(searchResponse.getHits().getHits())
                .forEach(hit -> documents.add(hit.getSourceAsMap()));

        return documents;
    }

    public List<Map<String, Object>> searchBasedOnFilterAndSort(
            String indexName,
            String filterField,
            String filterValue,
            String sortField,
            String sortOrder,
            int from,
            int size)
            throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (filterField != null && filterValue != null) {
            searchSourceBuilder.query(QueryBuilders.matchQuery(filterField, filterValue));
        }

        if (sortField != null) {
            searchSourceBuilder.sort(sortField, SortOrder.fromString(sortOrder));
        }

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return extractSearchResults(searchResponse);
    }

    private List<Map<String, Object>> extractSearchResults(SearchResponse searchResponse) {
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .collect(Collectors.toList());
    }

    public GetDataResponse getTotalSalesAndProfit(String indexName, String fromDate, String toDate, String productCategory) throws IOException {

        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        RangeQueryBuilder dateRangeQuery = QueryBuilders.rangeQuery("Date").gte(formatDate(fromDate)).lte(formatDate(toDate));
        TermQueryBuilder categoryQuery = QueryBuilders.termQuery("ProductCategory.keyword", productCategory);

        boolQueryBuilder.must(dateRangeQuery);
        boolQueryBuilder.must(categoryQuery);
        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.aggregation(AggregationBuilders.count("count").field("ProductCategory.keyword"));

        searchSourceBuilder.aggregation(AggregationBuilders.sum("total").field("TotalProfit"));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        long count = searchResponse.getAggregations().get("count") != null ? ((org.elasticsearch.search.aggregations.metrics.ValueCount)
                                        searchResponse.getAggregations().get("count")).getValue() : 0;

        double total = searchResponse.getAggregations().get("total") != null ?
                ((Sum) searchResponse.getAggregations().get("total")).getValue() : 0;

        GetDataResponse response = new GetDataResponse();
        response.setData(extractSearchResults(searchResponse));
        response.setSum((long) total);
        response.setCount(count);
        return response;
    }

    private String formatDate(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return localDate.format(DateTimeFormatter.ISO_DATE);
    }
}
