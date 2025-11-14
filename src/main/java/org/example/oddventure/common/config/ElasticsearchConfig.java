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

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        // connectedTo()는 호스트명만 받으므로 프로토콜 제거
        String host = elasticsearchUris
                .replace("https://", "")
                .replace("http://", "");

        var builder = ClientConfiguration.builder()
                .connectedTo(host)
                .usingSsl();  // HTTPS 통신 사용

        // username과 password가 모두 존재하는 경우에만 인증 설정
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        return builder.build();
    }
}