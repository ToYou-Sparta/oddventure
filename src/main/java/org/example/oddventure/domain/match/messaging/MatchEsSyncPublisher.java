package org.example.oddventure.domain.match.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.match.dto.event.MatchEsSyncEvent;
import org.example.oddventure.domain.match.sync.AdaptiveEsSyncManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Match 데이터 변경 시 Elasticsearch 동기화 이벤트 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEsSyncPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AdaptiveEsSyncManager syncManager;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PENDING_SYNC_KEY = "es:sync:pending:";

    /**
     * Match 생성 이벤트 발행
     */
    public void publishMatchCreated(Long matchId) {
        // 대용량 모드 확인
        if (!syncManager.shouldUseRabbitMQ()) {
            storePendingSync(matchId, MatchEsSyncEvent.SyncType.CREATE);
            return;
            //RabbitMQ 발행 안함
        }

        MatchEsSyncEvent event = new MatchEsSyncEvent(
                MatchEsSyncEvent.SyncType.CREATE,
                matchId
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_CREATED_KEY,
                event
        );

        log.info("Match 생성 이벤트 발행 - matchId: {}", matchId);
    }

    /**
     * Match 업데이트 이벤트 발행
     */
    public void publishMatchUpdated(Long matchId) {
        // 대용량 모드 확인
        if (!syncManager.shouldUseRabbitMQ()) {
            storePendingSync(matchId, MatchEsSyncEvent.SyncType.UPDATE);
            return;
        }

        MatchEsSyncEvent event = new MatchEsSyncEvent(
                MatchEsSyncEvent.SyncType.UPDATE,
                matchId
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_UPDATED_KEY,
                event
        );

        log.info("Match 업데이트 이벤트 발행 - matchId: {}", matchId);
    }

    /**
     * Match 삭제 이벤트 발행
     */
    public void publishMatchDeleted(Long matchId) {
        // 대용량 모드 확인
        if (!syncManager.shouldUseRabbitMQ()) {
            storePendingSync(matchId, MatchEsSyncEvent.SyncType.DELETE);
            return;
        }

        MatchEsSyncEvent event = new MatchEsSyncEvent(
                MatchEsSyncEvent.SyncType.DELETE,
                matchId
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ES_SYNC_EXCHANGE,
                RabbitMQConfig.ES_SYNC_DELETED_KEY,
                event
        );

        log.info("Match 삭제 이벤트 발행 - matchId: {}", matchId);
    }

    /**
     * 대용량 모드에서 동기화 대기 목록에 저장
     */
    private void storePendingSync(Long matchId, MatchEsSyncEvent.SyncType syncType) {
        String key = PENDING_SYNC_KEY + syncType.name().toLowerCase();
        redisTemplate.opsForSet().add(key, matchId);
        redisTemplate.expire(key, Duration.ofHours(1));  // 1시간 TTL

        log.info("대용량 모드 - 동기화 대기 저장: {} (matchId: {})", syncType, matchId);
    }

    /**
     * 대기 중인 동기화 목록 조회
     */
    public Set<Object> getPendingSyncIds(MatchEsSyncEvent.SyncType syncType) {
        String key = PENDING_SYNC_KEY + syncType.name().toLowerCase();
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 대기 중인 동기화 목록 삭제
     */
    public void clearPendingSync(MatchEsSyncEvent.SyncType syncType) {
        String key = PENDING_SYNC_KEY + syncType.name().toLowerCase();
        redisTemplate.delete(key);
    }
}
