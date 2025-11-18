package org.example.oddventure.domain.match.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 대용량 데이터 유입을 감지하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdaptiveEsSyncManager {

    private final RedisTemplate<String, Object> redisTemplate;

    // 임계값 설정
    private static final int BULK_THRESHOLD = 100;  // 1분에 100건 이상이면 대용량으로 간주
    private static final int TIME_WINDOW_SECONDS = 60;  // 1분 시간 윈도우
    private static final String COUNTER_KEY = "es:sync:counter";
    private static final String BULK_MODE_KEY = "es:sync:bulk_mode";

    // 로컬 카운터 (성능 최적화)
    private final AtomicInteger localCounter = new AtomicInteger(0);

    /**
     * 동기화 이벤트 발생 시 호출
     * @return true: RabbitMQ 사용, false: 사용안함
     */
    public boolean shouldUseRabbitMQ() {
        // 이미 대용량 모드인지 확인
        Boolean isBulkMode = (Boolean) redisTemplate.opsForValue().get(BULK_MODE_KEY);

        if (Boolean.TRUE.equals(isBulkMode)) {
            log.debug("대용량 모드 활성화 상태");
            return false;
        }

        // 로컬 카운터 증가
        int currentCount = localCounter.incrementAndGet();

        // 10건마다 Redis 동기화 (성능 최적화)
        if (currentCount % 10 == 0) {
            Long redisCount = redisTemplate.opsForValue().increment(COUNTER_KEY);
            redisTemplate.expire(COUNTER_KEY, Duration.ofSeconds(TIME_WINDOW_SECONDS));

            // 임계값 초과 시 대용량 모드 활성화
            if (redisCount != null && redisCount >= BULK_THRESHOLD) {
                activateBulkMode();
                return false;
            }
        }

        return true;
    }

    /**
     * 대용량 모드 활성화
     */
    private void activateBulkMode() {
        redisTemplate.opsForValue().set(BULK_MODE_KEY, true);
        localCounter.set(0);
        redisTemplate.delete(COUNTER_KEY);

        log.warn("=== 대용량 모드 활성화 ===");
    }

    /**
     * 대용량 모드 비활성화
     */
    public void deactivateBulkMode() {
        redisTemplate.delete(BULK_MODE_KEY);
        redisTemplate.delete(COUNTER_KEY);
        localCounter.set(0);

        log.info("=== 대용량 모드 비활성화 ===");
    }

    /**
     * 현재 대용량 모드 여부 확인
     */
    public boolean isBulkMode() {
        Boolean isBulkMode = (Boolean) redisTemplate.opsForValue().get(BULK_MODE_KEY);
        return Boolean.TRUE.equals(isBulkMode);
    }
}
