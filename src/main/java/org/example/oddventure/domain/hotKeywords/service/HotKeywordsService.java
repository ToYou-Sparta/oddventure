package org.example.oddventure.domain.hotKeywords.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class HotKeywordsService {
    private final HotKeywordsRepository hotKeywordsRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public HotKeywordsResponse getHotKeywords() {
        String key = "match:ranking";
        Set<Object> top5 = redisTemplate.opsForZSet().reverseRange(key, 0, 4);
        return HotKeywordsResponse.of(top5);
    }

    @Transactional
    @Scheduled(cron = "0 */5 * * * * ") //5분마다 db에 업데이트
    public void getAllViewCountForRdb() {
        String key = "match:ranking"; //zset은 Spring에서 TypedTuple로 표현됨.
        // 전체 멤버 + 점수 (낮은 점수부터 높은 점수까지)
        Set<ZSetOperations.TypedTuple<Object>> all = redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        if (all != null) {
            for (ZSetOperations.TypedTuple<Object> tuple : all) {
                String keyword = (String) tuple.getValue(); // 상품명 (member)
                Double score = tuple.getScore();            // 조회수 (score)
                hotKeywordsRepository.increaseViewCountByValue(keyword, score);
            }
        }
    }
}
