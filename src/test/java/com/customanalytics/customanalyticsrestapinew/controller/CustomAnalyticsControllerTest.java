package com.customanalytics.customanalyticsrestapinew.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.customanalytics.customanalyticsrestapinew.service.CustomAnalyticsService;
import com.customanalytics.customanalyticsrestapinew.service.UserService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
public class CustomAnalyticsControllerTest {
    @Autowired private MockMvc mockMvc;

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
}
