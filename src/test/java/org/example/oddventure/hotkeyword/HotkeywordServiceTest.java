package org.example.oddventure.hotkeyword;

import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.entity.HotKeywords;
import org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class HotkeywordServiceTest extends RedisTestContainerConfig {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotKeywordsRepository hotKeywordsRepository;

    @Autowired
    private HotKeywordsService hotKeywordsService;

    Map<String,Double> keyword;

    @BeforeEach
     void setUp() {
        ZSetOperations<String, Object> zSet = redisTemplate.opsForZSet();
        keyword = Map.of(
                "korea",6.0,
                "america",5.0,
                "china",4.0,
                "india",3.0,
                "england",2.0,
                "japan",1.0
        );
        keyword.forEach((k,v)-> zSet.add("match:ranking", k, v));

        hotKeywordsRepository.saveAll(
                keyword.keySet().stream()
                        .map(HotKeywords::new)
                        .toList());
    }

    @Test
    public void 인기_검색어_Top5를_조회할_수_있다() {
        // given
        // forEach로 대체

        // when
        HotKeywordsResponse hotKeywordsResponse = hotKeywordsService.getHotKeywords();

        // then
        assertThat(hotKeywordsResponse).isNotNull();
        assertThat(hotKeywordsResponse.getHotKeywords()).containsExactly("korea", "america", "china", "india", "england");
    }

    @Test
    public void 캐시_데이터를_DB에_저장할_수_있다() {
        // given
        // forEach로 대체

        // when
        hotKeywordsService.getAllViewCountForRdb();

        // then
        List<HotKeywords> result = hotKeywordsRepository.findAll();
        assertThat(result).isNotNull();
        result.forEach(hotKeywords -> {
            assertEquals(hotKeywords.getSearchCount(),keyword.get(hotKeywords.getKeyword()));
        });
    }
}
