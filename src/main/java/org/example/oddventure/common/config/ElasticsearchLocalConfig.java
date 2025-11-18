package org.example.oddventure.common.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Profile({"local", "dev"})
@Configuration
@EnableElasticsearchRepositories(basePackages = "org.example.oddventure.domain.match.repository.elasticsearch")
public class ElasticsearchLocalConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        String host = elasticsearchUri
                .replace("https://", "")
                .replace("http://", "")
                .split(":")[0];

        int port = elasticsearchUri.contains(":443") ? 443 : 9200;
        String scheme = elasticsearchUri.startsWith("https") ? "https" : "http";

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, scheme))
        );
    }

    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}
