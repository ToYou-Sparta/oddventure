package org.example.oddventure.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.example.oddventure.domain.ai.subscriber.ChatMessageOutputSubscriber;
import org.example.oddventure.domain.ai.subscriber.ChatMessageSubscriber;
import org.example.oddventure.domain.event.RedisSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    // 응답용 ObjectMapper (HTTP 응답, @class 없음)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    // Redis 캐싱용 ObjectMapper (@class 포함)
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(pageModule());

        // Redis 캐싱에만 타입 정보 추가
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;
    }

    // CacheManager (@Cacheable 어노테이션용)
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    // Redis용 ObjectMapper를 주입받아 RedisTemplate 생성
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }

    // --- Redis Pub/Sub 설정 ---

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisSubscriber redisSubscriber,
            ChatMessageSubscriber chatMessageSubscriber,
            ChatMessageOutputSubscriber chatMessageOutputSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(redisSubscriber, new PatternTopic("match:*:odds"));
        container.addMessageListener(redisSubscriber, new PatternTopic("match:*:info"));
        container.addMessageListener(chatMessageSubscriber, new PatternTopic("chat:*:input"));
        container.addMessageListener(chatMessageOutputSubscriber, new PatternTopic("chat:*:output"));

        return container;
    }

    // --- Page/Sort Mixin Helper ---

    // PageImpl, PageRequest, Sort 역직렬화를 위한 Jackson Mixin 모듈
    private Module pageModule() {
        SimpleModule module = new SimpleModule();
        module.setMixInAnnotation(PageImpl.class, PageImplMixin.class);
        module.setMixInAnnotation(PageRequest.class, PageRequestMixin.class);
        module.setMixInAnnotation(Sort.class, SortMixin.class);
        return module;
    }

    // PageImpl 역직렬화용 Mixin
    private abstract static class PageImplMixin<T> {
        @JsonCreator
        PageImplMixin(
                @JsonProperty("content") List<T> content,
                @JsonProperty("pageable") Pageable pageable,
                @JsonProperty("totalElements") long totalElements) {
        }
    }

    // PageRequest 역직렬화용 Mixin
    private abstract static class PageRequestMixin {
        @JsonCreator
        static PageRequest of(
                @JsonProperty("page") int page,
                @JsonProperty("size") int size,
                @JsonProperty("sort") Sort sort) {
            return PageRequest.of(page, size, sort);
        }
    }

    // Sort 역직렬화용 Mixin (unsorted 처리 포함)
    private abstract static class SortMixin {
        @JsonCreator
        static Sort by(@JsonProperty("orders") List<Sort.Order> orders) {
            if (orders == null || orders.isEmpty()) {
                return Sort.unsorted();
            }
            return Sort.by(orders);
        }
    }
}