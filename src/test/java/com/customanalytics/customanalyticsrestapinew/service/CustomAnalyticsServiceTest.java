package com.customanalytics.customanalyticsrestapinew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.customanalytics.customanalyticsrestapinew.contract.response.GetDataResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.elasticsearch.action.index.IndexRequest;

public class CustomAnalyticsServiceTest {
    private RestHighLevelClient mockClient;
    private CustomAnalyticsService customAnalyticsService;

    @Captor
    private ArgumentCaptor<IndexRequest> indexRequestCaptor;
    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        mockClient = mock(RestHighLevelClient.class);
        IndicesClient mockIndicesClient = mock(IndicesClient.class);
        when(mockClient.indices()).thenReturn(mockIndicesClient);
        customAnalyticsService = new CustomAnalyticsService(mockClient);
    }

    @Test
    public void testUploadFile() throws Exception {

        String indexName = "test1";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test.csv");
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", inputStream);

        when(mockClient.indices().exists(any(GetIndexRequest.class), any())).thenReturn(false);

        List<String[]> rows = List.of(
                new String[]{"Header1", "Header2"},
                new String[]{"123", "test"}
        );

        customAnalyticsService.uploadFile(indexName, file);

        verify(mockClient.indices(), times(1)).exists(any(GetIndexRequest.class), any());

        List<IndexRequest> capturedIndexRequests = indexRequestCaptor.getAllValues();
        for (IndexRequest capturedRequest : capturedIndexRequests) {
            Map<String, Object> actualSource = capturedRequest.sourceAsMap();
            Map<String, Object> expectedDataMap = new HashMap<>();
            expectedDataMap.put("Header1", "123");
            expectedDataMap.put("Header2", "test");
            assertEquals(expectedDataMap, actualSource);

        }
    }
    @Test
    public void testGetDataByIndexName() throws IOException {
        String indexName = "testIndex";
        List<Map<String, Object>> expectedDocuments = new ArrayList<>();
        expectedDocuments.add(Collections.singletonMap("key", "value"));
        when(mockClient.indices().exists(any(GetIndexRequest.class), any(RequestOptions.class)))
                .thenReturn(true);
        SearchResponse searchResponse = mock(SearchResponse.class);
        SearchHits searchHits = mock(SearchHits.class);
        SearchHit[] searchHitArray = {mock(SearchHit.class)};
        when(searchHitArray[0].getSourceAsMap())
                .thenReturn(Collections.singletonMap("key", "value"));
        when(searchHits.getHits()).thenReturn(searchHitArray);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(mockClient.search(any(SearchRequest.class), any(RequestOptions.class)))
                .thenReturn(searchResponse);
        List<?> result = customAnalyticsService.getDataByIndexName(indexName);

        assertEquals(expectedDocuments, result);
    }

    @Test
    public void testSearchBasedOnFilterAndSort() throws IOException {
        String indexName = "test";
        String filterField = "GeographicLocation";
        String filterValue = "NewYork";
        String sortField = "Date";
        String sortOrder = "ASC";
        int from = 0;
        int size = 10;
        SearchHit mockSearchHit1 = mock(SearchHit.class);
        when(mockSearchHit1.getSourceAsMap()).thenReturn(Map.of(filterField, filterValue));

        SearchHits mockSearchHits = mock(SearchHits.class);
        when(mockSearchHits.getHits()).thenReturn(new SearchHit[] {mockSearchHit1});

        SearchResponse mockSearchResponse = mock(SearchResponse.class);
        when(mockSearchResponse.getHits()).thenReturn(mockSearchHits);

        when(mockClient.search(
                        ArgumentMatchers.any(SearchRequest.class),
                        ArgumentMatchers.any(RequestOptions.class)))
                .thenReturn(mockSearchResponse);
        List<Map<String, Object>> result =
                customAnalyticsService.searchBasedOnFilterAndSort(
                        indexName, filterField, filterValue, sortField, sortOrder, from, size);
        assertEquals(1, result.size());
        assertEquals("NewYork", result.get(0).get("GeographicLocation"));
    }

    @Test
    public void testGetTotalSalesAndProfit() throws IOException {

        SearchResponse searchResponse = mock(SearchResponse.class);
        Aggregations aggregations = mock(Aggregations.class);
        when(searchResponse.getAggregations()).thenReturn(aggregations);

        ValueCount countAggregation = mock(ValueCount.class);
        when(countAggregation.getValue()).thenReturn(10L);
        when(aggregations.get("count")).thenReturn(countAggregation);

        Sum totalAggregation = mock(Sum.class);
        when(totalAggregation.getValue()).thenReturn(500.0);
        when(aggregations.get("total")).thenReturn(totalAggregation);

        SearchHit hit1 = mock(SearchHit.class);
        when(hit1.getId()).thenReturn("1");
        when(hit1.getSourceAsString()).thenReturn("{\"ProductCategory\": \"count\"}");
        SearchHit hit2 = mock(SearchHit.class);
        when(hit2.getId()).thenReturn("2");
        when(hit2.getSourceAsString()).thenReturn("{\"TotalProfit\": \"total\"}");

        SearchHits hits = mock(SearchHits.class);
        when(hits.getHits()).thenReturn(new SearchHit[]{hit1, hit2});
        when(searchResponse.getHits()).thenReturn(hits);

        when(mockClient.search(any(SearchRequest.class), any(RequestOptions.class))).thenReturn(searchResponse);

        GetDataResponse response = customAnalyticsService.getTotalSalesAndProfit("yourIndex", "2023-01-01", "2023-12-31", "yourCategory");

        assertEquals(10L, response.getCount());
        assertEquals(500L, response.getSum());
        assertEquals(2, response.getData().size());
    }
}
