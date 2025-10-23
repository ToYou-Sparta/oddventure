package org.example.oddventure.domain.hotKeywords.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotKeywordsService {

    private final static String HOT_KEYWORDS_KEY = "match:ranking";
    private final HotKeywordsRepository hotKeywordsRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public HotKeywordsResponse getHotKeywords() {
        Set<Object> top5 = redisTemplate.opsForZSet().reverseRange(HOT_KEYWORDS_KEY, 0, 4);
        return HotKeywordsResponse.of(top5);
    }

    @Scheduled(cron = "0 */5 * * * * ") //5분마다 db에 업데이트
    public void getAllSearchCountForRdb() {
        String key = HOT_KEYWORDS_KEY; //zset은 Spring에서 TypedTuple로 표현됨.
        // 전체 멤버 + 점수 (낮은 점수부터 높은 점수까지)
        Set<ZSetOperations.TypedTuple<Object>> all = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        if (all != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : all) {
                String keyword = (String) tuple.getValue(); // 경기 이름 (keyword)
                Double score = tuple.getScore();            // 조회수 (score)
                hotKeywordsRepository.increaseSearchCountByValue(keyword, score);
            }
        }
    }
}