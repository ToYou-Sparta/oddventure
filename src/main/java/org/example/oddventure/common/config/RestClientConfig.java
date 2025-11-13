package org.example.oddventure.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean(name = "steamClient")
    public RestClient steamClient(
            @Value("${steam.api.base-url:https://api.steampowered.com}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
