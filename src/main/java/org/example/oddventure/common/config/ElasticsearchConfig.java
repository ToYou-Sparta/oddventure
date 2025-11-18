package org.example.oddventure.common.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * AWS OpenSearch 설정 (Elasticsearch 7.17 버전 사용)
 * RestHighLevelClient 기반
 */
@Configuration
@Profile("prod")
@EnableElasticsearchRepositories(basePackages = "org.example.oddventure.domain.match.repository.elasticsearch")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    @Primary
    @Override
    public RestHighLevelClient elasticsearchClient() {
        // URI에서 호스트와 포트 추출
        String cleanUri = elasticsearchUris
                .replace("https://", "")
                .replace("http://", "");

        String hostname;
        int port = 443; // AWS OpenSearch 기본 포트

        if (cleanUri.contains(":")) {
            String[] parts = cleanUri.split(":");
            hostname = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            hostname = cleanUri;
        }

        String scheme = elasticsearchUris.startsWith("https") ? "https" : "http";

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme))
                        .setHttpClientConfigCallback(httpClientBuilder ->
                                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        )
                        .setRequestConfigCallback(requestConfigBuilder ->
                                requestConfigBuilder
                                        .setConnectTimeout(5000)
                                        .setSocketTimeout(60000)
                        )
        );
    }

    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}