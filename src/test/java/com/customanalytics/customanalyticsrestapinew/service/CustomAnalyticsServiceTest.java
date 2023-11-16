package com.customanalytics.customanalyticsrestapinew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

public class CustomAnalyticsServiceTest {
    private RestHighLevelClient mockClient;

    private CustomAnalyticsService customAnalyticsService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        mockClient = mock(RestHighLevelClient.class);
        IndicesClient mockIndicesClient = mock(IndicesClient.class);
        when(mockClient.indices()).thenReturn(mockIndicesClient);
        customAnalyticsService = new CustomAnalyticsService(mockClient);
    }

    @Test
    void testUploadFile() throws IOException {
        String indexName = "test1";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "test.csv", "text/plain", "name,age\nJohn,30\nJane,25".getBytes());

        when(mockClient.indices().exists(any(GetIndexRequest.class), any())).thenReturn(false);
        when(mockClient.indices().create(any(CreateIndexRequest.class), any())).thenReturn(null);
        when(mockClient.bulk(any(BulkRequest.class), any())).thenReturn(null);

        customAnalyticsService.uploadFile(indexName, file);

        verify(mockClient.indices(), times(1)).exists(any(GetIndexRequest.class), any());

        verify(mockClient, times(1)).bulk(any(), any());
        assertTrue(true);
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
        int from =0;
        int size =10;
        SearchHit mockSearchHit1 = mock(SearchHit.class);
        when(mockSearchHit1.getSourceAsMap()).thenReturn(Map.of(filterField, filterValue));

        SearchHits mockSearchHits = mock(SearchHits.class);
        when(mockSearchHits.getHits()).thenReturn(new SearchHit[]{mockSearchHit1});

        SearchResponse mockSearchResponse = mock(SearchResponse.class);
        when(mockSearchResponse.getHits()).thenReturn(mockSearchHits);

        when(mockClient.search(ArgumentMatchers.any(SearchRequest.class), ArgumentMatchers.any(RequestOptions.class)))
                .thenReturn(mockSearchResponse);
        List<Map<String, Object>> result = customAnalyticsService.searchBasedOnFilterAndSort(
                indexName, filterField, filterValue, sortField, sortOrder, from, size);
        assertEquals(1, result.size());
        assertEquals("NewYork", result.get(0).get("GeographicLocation"));
    }
}
