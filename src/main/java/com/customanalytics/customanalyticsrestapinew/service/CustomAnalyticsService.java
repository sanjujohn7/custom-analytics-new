package com.customanalytics.customanalyticsrestapinew.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.customanalytics.customanalyticsrestapinew.contract.getDataResponse;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
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
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.elasticsearch.search.SearchHit;

@Service
@RequiredArgsConstructor
public class CustomAnalyticsService {

    private final RestHighLevelClient client;

    public void uploadFile(String indexName, MultipartFile file) throws IOException {
        List<Map<String, String>> records;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = br.readLine().split(",");
            // Remove spaces from headers
            String[] cleanedHeaders = Arrays.stream(headers)
                    .map(String::trim) // Remove leading and trailing spaces
                    .map(header -> header.replaceAll("\\s+", "")) // Remove spaces within the header
                    .toArray(String[]::new);

            records = br.lines()
                    .map(s -> s.split(","))
                    .map(t -> IntStream.range(0, t.length).boxed().collect(toMap(i -> cleanedHeaders[i], i -> t[i])))
                    .collect(toList());
        }
        if (indexExists(indexName)) {
            throw new RuntimeException("Index name already Exist");
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

    public List<?> getDataByIndexName(String indexName) throws IOException {
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
            return List.of("Index does not exist");
        }
    }

    private List<Map<String, Object>> extractDocuments(SearchResponse searchResponse) {
        List<Map<String, Object>> documents = new ArrayList<>();

        Arrays.stream(searchResponse.getHits().getHits())
                .forEach(hit -> documents.add(hit.getSourceAsMap()));

        return documents;
    }

    public List<Map<String, Object>> searchBasedOnFilterAndSort(String indexName, String filterField, String filterValue, String sortField, String sortOrder,
                        int from,int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // Add filter
        if (filterField != null && filterValue != null) {
            searchSourceBuilder.query(QueryBuilders.matchQuery(filterField, filterValue));
        }

        // Add sorting
        if (sortField != null) {
            searchSourceBuilder.sort(sortField, SortOrder.fromString(sortOrder));
        }

        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return extractSearchResults(searchResponse);
    }

    public getDataResponse getDataBetweenDatesAndCategory(String indexName, String fromDate, String toDate, String productCategory) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        RangeQueryBuilder dateRangeQuery = QueryBuilders.rangeQuery("Date")
                .gte(formatDate(fromDate))
                .lte(formatDate(toDate));

        TermQueryBuilder categoryQuery = QueryBuilders.termQuery("ProductCategory.keyword", productCategory);

        boolQueryBuilder.must(dateRangeQuery);
        boolQueryBuilder.must(categoryQuery);

        searchSourceBuilder.query(boolQueryBuilder);


        searchSourceBuilder.aggregation(AggregationBuilders.count("count").field("ProductCategory.keyword")); // Replace 'field_name' with your actual field name

        //searchSourceBuilder.aggregation(AggregationBuilders.sum("total").field("TotalProfit.numeric")); // Replace 'numeric_field' with your actual numeric field name

        searchSourceBuilder.aggregation(AggregationBuilders.sum("sum_agg")
                .script(new Script("Double.parseDouble(doc[TotalProfit].value)")));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        long count = searchResponse.getAggregations().get("count") != null ?
                ((org.elasticsearch.search.aggregations.metrics.ValueCount) searchResponse.getAggregations().get("count")).getValue() : 0;

        double total = searchResponse.getAggregations().get("total") != null ?
                ((Sum) searchResponse.getAggregations().get("total")).getValue() : 0;


        getDataResponse response = new getDataResponse();
        response.setData(extractSearchResults(searchResponse));
        response.setSum(total);
        response.setCount(count);
        return response;
    }

    private String formatDate(String date) {
        // You may need to adjust the date format based on your Elasticsearch date mapping
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        return localDate.format(DateTimeFormatter.ISO_DATE);
    }

    private List<Map<String, Object>> extractSearchResults(SearchResponse searchResponse) {
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .collect(Collectors.toList());
    }
}
