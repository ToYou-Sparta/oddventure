package org.example.oddventure.common.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
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
        WebClient build = WebClient.builder()
                .baseUrl(GRID_CENTRAL_BASE_URL)
                .defaultHeader("x-api-key", GRID_API_KEY)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return build;
    }

    @Bean
    public WebClient gridLiveClient() {
        return WebClient.builder()
                .baseUrl(GRID_LIVE_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + GRID_API_KEY)
                .build();
    }
}
