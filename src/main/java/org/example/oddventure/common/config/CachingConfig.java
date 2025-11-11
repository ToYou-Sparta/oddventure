package org.example.oddventure.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CachingConfig {

    // RedisConfig에서 생성한 공용 ObjectMapper를 주입

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory cf,
                                               ObjectMapper redisObjectMapper) { // ObjectMapper 주입

        // 주입받은 ObjectMapper로 Serializer 생성
        var valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        var base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put("match:ranking", base.entryTtl(Duration.ofMinutes(30)));
        perCache.put("matchDetails", base.entryTtl(Duration.ofMinutes(30)));
        perCache.put("teams", base.entryTtl(Duration.ofMinutes(30)));
        perCache.put("teamDetail", base.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(perCache)
                .enableStatistics()
                .build();
    }
}