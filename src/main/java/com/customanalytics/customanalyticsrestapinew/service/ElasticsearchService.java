package com.customanalytics.customanalyticsrestapinew.service;



import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final RestHighLevelClient client;


    public void uploadFile(String indexName, MultipartFile file) throws IOException {
        List<Map<String, String>> records;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = br.readLine().split(",");
            records = br.lines().map(s -> s.split(","))
                    .map(t -> IntStream.range(0, t.length)
                            .boxed()
                            .collect(toMap(i -> headers[i], i -> t[i])))
                    .collect(toList());
            System.out.println(headers);
            System.out.println(records);
        }
        ;
        if (!indexExists(indexName)) {
            createIndex(indexName);
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (Map<String, String> record : records) {
            bulkRequest.add(new IndexRequest(indexName).source(record));
        }
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }
        private boolean indexExists (String indexName) throws IOException {
            GetIndexRequest request = new GetIndexRequest(indexName);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        }

    private void createIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        client.indices().create(request, RequestOptions.DEFAULT);

    }
}