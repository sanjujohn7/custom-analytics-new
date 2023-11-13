package com.customanalytics.customanalyticsrestapinew;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomAnalyticsRestApiNewApplication {



	public static void main(String[] args) {
		SpringApplication.run(CustomAnalyticsRestApiNewApplication.class, args);
	}

}
