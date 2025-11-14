package org.example.oddventure.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        // connectedTo()는 호스트명:포트 형식으로 받음
        String host = elasticsearchUris
                .replace("https://", "")
                .replace("http://", "");

        // 포트가 없으면 443 추가 (AWS Elasticsearch는 HTTPS 443 포트 사용)
        if (!host.contains(":")) {
            host = host + ":443";
        }

        var builder = ClientConfiguration.builder()
                .connectedTo(host)
                .usingSsl()  // HTTPS 통신 사용
                .withConnectTimeout(java.time.Duration.ofSeconds(5))
                .withSocketTimeout(java.time.Duration.ofSeconds(30))
                .withHeaders(() -> {
                    // Elasticsearch 7.x와 8.x 클라이언트 호환성을 위한 헤더
                    var headers = new org.springframework.data.elasticsearch.support.HttpHeaders();
                    headers.add("Accept", "application/vnd.elasticsearch+json;compatible-with=7");
                    headers.add("Content-Type", "application/vnd.elasticsearch+json;compatible-with=7");
                    return headers;
                });

        // username과 password가 모두 존재하는 경우에만 Basic Auth 설정
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        return builder.build();
    }
}