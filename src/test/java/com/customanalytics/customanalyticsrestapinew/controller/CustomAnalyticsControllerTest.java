package com.customanalytics.customanalyticsrestapinew.controller;


import com.customanalytics.customanalyticsrestapinew.service.CustomAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CustomAnalyticsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomAnalyticsService customAnalyticsService;

    @Test
    public void testUploadFile() throws Exception {

        //Given
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", "file content".getBytes());
        String indexName = "test";
        //When
        doNothing().when(customAnalyticsService).uploadFile(eq(indexName),any(MultipartFile.class));

        //Then
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("indexName", indexName))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFileException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/plain", "file content".getBytes());
        String indexName = "test";

        // Mock the behavior of void method using doThrow for IOException
        doThrow(new IOException("Error uploading file")).when(customAnalyticsService).uploadFile(eq(indexName), any(MultipartFile.class));

        // When and Then
        mockMvc.perform(multipart("/upload")
                        .file(file)
                        .param("indexName", indexName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error uploading file: Error uploading file"));
    }

}
