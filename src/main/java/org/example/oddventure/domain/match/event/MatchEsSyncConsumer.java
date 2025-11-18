package org.example.oddventure.domain.match.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.dto.event.MatchEsSyncEvent;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.repository.elasticsearch.MatchSearchRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 동기화 메시지 수신 및 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEsSyncConsumer {

    private final MatchRepository matchRepository;  // MySQL
    private final MatchSearchRepository matchSearchRepository;  // Elasticsearch

    /**
     * RabbitMQ에서 메시지 수신 후 Elasticsearch 동기화
     */
    @RabbitListener(queues = RabbitMQConfig.ES_SYNC_QUEUE)
    public void handleEsSyncEvent(MatchEsSyncEvent event) {
        log.info("Elasticsearch 동기화 이벤트 수신 - type: {}, matchId: {}",
                event.syncType(), event.matchId());

        try {
            switch (event.syncType()) {
                case CREATE, UPDATE -> syncMatchToEs(event.matchId());
                case DELETE -> deleteMatchFromEs(event.matchId());
            }
        } catch (Exception e) {
            log.error("Elasticsearch 동기화 실패 - matchId: {}", event.matchId(), e);
            // 실패 시 재시도 로직 또는 Dead Letter Queue로 보낼 수 있음
        }
    }

    /**
     * MySQL에서 Match 조회 후 Elasticsearch에 저장/업데이트
     */
    private void syncMatchToEs(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        MatchDocument document = MatchDocument.from(match);
        matchSearchRepository.save(document);

        log.info("Elasticsearch 동기화 완료 - matchId: {}", matchId);
    }

    /**
     * Elasticsearch에서 Match 삭제
     */
    private void deleteMatchFromEs(Long matchId) {
        matchSearchRepository.deleteById(String.valueOf(matchId));
        log.info("Elasticsearch에서 삭제 완료 - matchId: {}", matchId);
    }
}
