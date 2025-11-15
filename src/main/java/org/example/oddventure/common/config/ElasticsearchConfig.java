package org.example.oddventure.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * AWS OpenSearch 설정 (Elasticsearch 클라이언트 사용)
 * OpenSearch는 Elasticsearch 7.x REST API와 호환됨
 */
@Configuration
@Profile("prod")  // 프로덕션 환경에서만 활성화
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    @Primary  // 기본 Elasticsearch 클라이언트를 OpenSearch용으로 대체
    public ElasticsearchClient elasticsearchClient() {
        // URI에서 호스트와 포트 추출
        String host = elasticsearchUris
                .replace("https://", "")
                .replace("http://", "");

        // 포트 분리
        String hostname;
        int port = 443; // AWS OpenSearch 기본 포트

        if (host.contains(":")) {
            String[] parts = host.split(":");
            hostname = parts[0];
            port = Integer.parseInt(parts[1]);
        } else {
            hostname = host;
        }

        // HTTPS 사용 여부 확인
        String scheme = elasticsearchUris.startsWith("https") ? "https" : "http";

        // RestClient 빌더 생성
        var restClientBuilder = RestClient.builder(new HttpHost(hostname, port, scheme));

        // Master User 인증 설정
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
            );

            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        // RestClient 생성
        RestClient restClient = restClientBuilder.build();

        // ElasticsearchClient 생성 (OpenSearch와 호환)
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}