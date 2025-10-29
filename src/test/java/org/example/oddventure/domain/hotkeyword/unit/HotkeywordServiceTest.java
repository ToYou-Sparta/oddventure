package org.example.oddventure.domain.hotkeyword.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.entity.HotKeywords;
import org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class HotkeywordServiceTest extends RedisTestContainerConfig {

    Map<String, Double> keyword;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotKeywordsRepository hotKeywordsRepository;

    @Autowired
    private HotKeywordsService hotKeywordsService;

    @BeforeEach
    void setUp() {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();
        keyword = Map.of(
                "korea", 6.0,
                "america", 5.0,
                "china", 4.0,
                "india", 3.0,
                "england", 2.0,
                "japan", 1.0
        );
        keyword.forEach((k, v) -> zSet.add("match:ranking", k, v));

        hotKeywordsRepository.saveAll(
                keyword.keySet().stream()
                        .map(HotKeywords::new)
                        .toList());
    }

    @Test
    @DisplayName("인기 검색어 Top5를 조회할 수 있다.")
    void getTop5HotKeywords_success() {
        // given
        // forEach로 대체

        // when
        HotKeywordsResponse hotKeywordsResponse = hotKeywordsService.getHotKeywords();

        // then
        assertThat(hotKeywordsResponse).isNotNull();
        assertThat(hotKeywordsResponse.hotKeywords()).containsExactly("korea", "america", "china", "india",
                "england");
    }

    @Test
    @DisplayName("Redis의 검색 카운트를 DB에 동기화할 수 있다.")
    void syncSearchCountFromRedisToRdb_success() {
        // given
        // forEach로 대체

        // when
        hotKeywordsService.getAllSearchCountForRdb();

        // then
        List<HotKeywords> result = hotKeywordsRepository.findAll();
        assertThat(result).isNotNull();
        result.forEach(hotKeywords -> {
            assertEquals(hotKeywords.getSearchCount(), keyword.get(hotKeywords.getKeyword()));
        });
    }
}
