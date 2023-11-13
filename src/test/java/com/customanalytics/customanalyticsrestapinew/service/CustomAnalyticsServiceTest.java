package com.customanalytics.customanalyticsrestapinew.service;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;

import org.mockito.MockitoAnnotations;


import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;


import static org.junit.jupiter.api.Assertions.assertTrue;



public class CustomAnalyticsServiceTest {
  // @Mock
    private RestHighLevelClient mockClient;

    // @InjectMocks
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
        // Given
        String indexName = "test1";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", "name,age\nJohn,30\nJane,25".getBytes());



        when(mockClient.indices().exists(any(GetIndexRequest.class), any()))
                .thenReturn(false);
        when(mockClient.indices().create(any(CreateIndexRequest.class), any())).thenReturn(null);
        when(mockClient.bulk(any(BulkRequest.class), any())).thenReturn(null);

        // When
        customAnalyticsService.uploadFile(indexName, file);

        // Then
        verify(mockClient.indices(), times(1)).exists(any(GetIndexRequest.class), any());
        //verify(mockClient.indices(), times(1)).create(any(CreateIndexRequest.class), any());
        //verify(mockClient).create(Mockito.any(CreateIndexRequest.class), Mockito.eq(RequestOptions.DEFAULT));
        verify(mockClient, times(1)).bulk(any(), any());
        assertTrue(true);
    }
}
