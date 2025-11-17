package org.example.oddventure.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

/**
 * AWS OpenSearch 설정 (Elasticsearch 7.x 호환 모드)
 * OpenSearch는 Elasticsearch 7.10 기반으로 Elasticsearch 8.x Content-Type 미지원
 * 명시적으로 application/json 헤더 사용하여 406 에러 방지
 */
@Configuration
@Profile("prod")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    @Primary
    public RestClient restClient() {
        // URI 파싱
        String cleanUri = elasticsearchUris
                .replace("https://", "")
                .replace("http://", "");

        String hostname;
        int port = 443;

        if (cleanUri.contains(":")) {
            String[] parts = cleanUri.split(":");
            hostname = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            hostname = cleanUri;
        }

        String scheme = elasticsearchUris.startsWith("https") ? "https" : "http";

        // RestClient 빌더 생성
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(hostname, port, scheme)
        );

        // 인증 설정
        if (username != null && !username.isEmpty()) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
            );

            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        // OpenSearch 호환을 위한 헤더 설정
        // Elasticsearch 8.x의 "application/vnd.elasticsearch+json" 대신
        // 표준 "application/json" 사용
        builder.setDefaultHeaders(new Header[]{
                new BasicHeader("Accept", "application/json"),
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("X-Elastic-Product", "Elasticsearch")
        });

        // 타임아웃 설정 (선택사항)
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000)
        );

        return builder.build();
    }

    @Bean
    @Primary
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // RestClientTransport 생성
        return new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );
    }

    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    @Primary
    public ElasticsearchTemplate elasticsearchTemplate(
            ElasticsearchClient elasticsearchClient,
            ElasticsearchConverter elasticsearchConverter) {
        return new ElasticsearchTemplate(elasticsearchClient, elasticsearchConverter);
    }
}