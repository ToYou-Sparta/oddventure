package org.example.oddventure.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${grid.central-data.base-url}")
    private String GRID_CENTRAL_BASE_URL;

    @Value("${grid.live-data.base-url}")
    private String GRID_LIVE_BASE_URL;

    @Value("${grid.api-key}")
    private String GRID_API_KEY;

    @Bean
    public WebClient gridCentralClient() {
        return WebClient.builder()
                .baseUrl(GRID_CENTRAL_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + GRID_API_KEY)
                .build();
    }

    @Bean
    public WebClient gridLiveClient() {
        return WebClient.builder()
                .baseUrl(GRID_LIVE_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + GRID_API_KEY)
                .build();
    }
}
