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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
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

    public void uploadFile(String indexName, MultipartFile file) throws IOException {
        List<Map<String, String>> records;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = br.readLine().split(",");

            String[] cleanedHeaders = Arrays.stream(headers)
                    .map(String::trim)
                    .map(header -> header.replaceAll("\\s+", ""))
                    .toArray(String[]::new);

            records = br.lines()
                    .map(s -> s.split(","))
                    .map(t -> IntStream.range(0, t.length).boxed().collect(toMap(i -> cleanedHeaders[i], i -> t[i])))
                    .collect(toList());
        }
        if (indexExists(indexName)) {
            throw new IndexAlreadyExistException("Index name already Exist");
        }
        createIndex(indexName);
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, String> record : records) {
            bulkRequest.add(new IndexRequest(indexName).source(record));
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    private boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    private void createIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        client.indices().create(request, RequestOptions.DEFAULT);
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

        searchSourceBuilder.aggregation(AggregationBuilders.sum("total").field("TotalSales.numeric"));

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
