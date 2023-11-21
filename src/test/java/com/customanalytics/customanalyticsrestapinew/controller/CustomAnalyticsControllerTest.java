package com.customanalytics.customanalyticsrestapinew.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.customanalytics.customanalyticsrestapinew.contract.response.GetDataResponse;
import com.customanalytics.customanalyticsrestapinew.service.CustomAnalyticsService;
import com.customanalytics.customanalyticsrestapinew.service.UserService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class CustomAnalyticsControllerTest {
    @Autowired private MockMvc mockMvc;

    @Autowired
    private CustomAnalyticsController customAnalyticsController;

    @MockBean private CustomAnalyticsService customAnalyticsService;
    @MockBean private UserService userService;

    @Test
    public void testUploadFile() throws Exception {

        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv", "text/plain", "file content".getBytes());
        String indexName = "test";
        doNothing()
                .when(customAnalyticsService)
                .uploadFile(eq(indexName), any(MultipartFile.class));

        mockMvc.perform(
                        multipart("/custom-analytics/upload")
                                .file(file)
                                .param("indexName", indexName))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFileException() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv", "text/plain", "file content".getBytes());
        String indexName = "test";

        doThrow(new IOException("Error uploading file"))
                .when(customAnalyticsService)
                .uploadFile(eq(indexName), any(MultipartFile.class));

        mockMvc.perform(
                        multipart("/custom-analytics/upload")
                                .file(file)
                                .param("indexName", indexName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error uploading file: Error uploading file"));
    }

    @Test
    void testGetDataByIndexName_Success() throws Exception {
        String indexName = "yourIndex";
        List<?> mockData = Collections.singletonList("Test Data");

        Mockito.when(customAnalyticsService.getDataByIndexName(indexName)).thenReturn((List<Map<String, Object>>) mockData);

        mockMvc.perform(get("/custom-analytics/get")
                        .param("indexName", indexName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Test Data"));
    }

    @Test
    public void testGetDataByIndexNameException() throws Exception {
        String indexName = "test";
        doThrow(new IOException("Error retrieving data"))
                .when(customAnalyticsService)
                .getDataByIndexName(indexName);
        mockMvc.perform(get("/custom-analytics/get").param("indexName", indexName))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        content()
                                .string("[\"Error while retrieving data Error retrieving data\"]"));
    }

    @Test
    public void testSearch_Success() throws Exception {
        String indexName = "yourIndex";
        String filterField = "filterField";
        String filterValue = "filterValue";
        String sortField = "sortField";
        String sortOrder = "ASC";
        int from = 0;
        int size = 10;

        List<?> mockData = Collections.singletonList("Test Data");

        when(customAnalyticsService.searchBasedOnFilterAndSort(
                indexName, filterField, filterValue, sortField, sortOrder, from, size))
                .thenReturn((List<Map<String, Object>>) mockData);

        mockMvc.perform(get("/custom-analytics/search")
                        .param("indexName", indexName)
                        .param("filterField", filterField)
                        .param("filterValue", filterValue)
                        .param("sortField", sortField)
                        .param("sortOrder", sortOrder)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("Test Data"));
    }
    @Test
    public void testGetDataFilterException() throws Exception {
        String indexName = "test";
        String filterField = "Geographic Location";
        String filterValue = "New York";
        String sortField = "Date";
        String sortOrder = "ASC";
        int from = 0;
        int size = 10;
        doThrow(new IOException("Error filtering data"))
                .when(customAnalyticsService)
                .searchBasedOnFilterAndSort(
                        indexName, filterField, filterValue, sortField, sortOrder, from, size);
        mockMvc.perform(
                        get("/custom-analytics/search")
                                .param("indexName", indexName)
                                .param("filterField", filterField)
                                .param("filterValue", filterValue)
                                .param("sortField", sortField)
                                .param("sortOrder", sortOrder)
                                .param("from", String.valueOf(from))
                                .param("size", String.valueOf(size)))
                .andExpect(status().isInternalServerError())
                .andExpect(
                        content().string("[\"Error while filtering data Error filtering data\"]"));
    }

    @Test
    public void testGetTotalSalesAndProfit_Success() throws Exception {

        String indexName = "indexName";
        String fromDate = "2023-01-01";
        String toDate = "2023-01-01";
        String productCategory = "productCategory";
        GetDataResponse mockResponse = new GetDataResponse();

        when(customAnalyticsService.getTotalSalesAndProfit(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/custom-analytics/data")
                        .param("indexName", indexName)
                        .param("fromDate", fromDate)
                        .param("toDate", toDate)
                        .param("productCategory", productCategory)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());

        verify(customAnalyticsService).getTotalSalesAndProfit(indexName, fromDate, toDate, productCategory);
    }
}
