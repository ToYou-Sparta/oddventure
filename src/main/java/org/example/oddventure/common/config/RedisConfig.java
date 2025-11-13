package org.example.oddventure.common.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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


    // 캐시와 RedisTemplate이 공용으로 사용할 ObjectMapper 빈을 생성
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. LocalDateTime 직렬화 모듈
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Page 객체 역직렬화 모듈 (PageImpl, PageRequest, Sort)
        objectMapper.registerModule(pageModule());

        // 3. record 클래스(final)를 포함한 모든 객체에 타입 정보를 추가
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING, // NON_FINAL에서 변경
                JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;
    }


    // 공용 ObjectMapper를 주입받아 RedisTemplate을 생성
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                       ObjectMapper redisObjectMapper) { // ObjectMapper 주입
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 주입받은 ObjectMapper로 Serializer 생성
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

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
