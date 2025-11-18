package org.example.oddventure.domain.match.scheduler;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.event.MatchEsSyncPublisher;
import org.example.oddventure.domain.match.event.dto.MatchEsSyncEvent;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.match.repository.elasticsearch.MatchSearchRepository;
import org.example.oddventure.domain.match.sync.AdaptiveEsSyncManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL → Elasticsearch 동기화 스케줄러 (Spring Batch 기반)
 *
 * <p>주기적으로 Spring Batch Job을 실행하여 MySQL의 Match 데이터를 Elasticsearch에 동기화
 *
 * <p>기존 스케줄러 방식 대비 장점:
 * <ul>
 *   <li>자동 재시작: 실패 지점부터 재시작 가능</li>
 *   <li>트랜잭션 관리: Chunk 단위 커밋/롤백</li>
 *   <li>실행 이력: JobRepository에 모든 실행 이력 저장</li>
 *   <li>모니터링: JobExecution을 통한 상세 추적</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEsSyncScheduler {

    private final MatchRepository matchRepository;
    private final MatchSearchRepository matchSearchRepository;
    private final AdaptiveEsSyncManager syncManager;
    private final MatchEsSyncPublisher syncPublisher;
    private final JobLauncher jobLauncher;
    private final Job matchEsSyncJob;

    /**
     * MySQL → Elasticsearch 전체 동기화 (Spring Batch Job 실행) 매일 새벽 3시에 실행 (cron 표현식)
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncAllMatchesToElasticsearch() {
        log.info("=== MySQL → Elasticsearch 전체 동기화 Batch Job 시작 ===");

        try {
            // JobParameters에 타임스탬프를 추가하여 매번 새로운 Job 인스턴스 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(matchEsSyncJob, jobParameters);

            log.info("=== MySQL → Elasticsearch 전체 동기화 Batch Job 완료 ===");

        } catch (Exception e) {
            log.error("MySQL → Elasticsearch 전체 동기화 Batch Job 실패", e);
        }
    }

    /**
     * 대용량 모드 체크 및 즉시 동기화 5분마다 실행하여 대용량 모드 감지 시 즉시 동기화
     */
    @Scheduled(fixedDelay = 300000)  // 5분마다 실행
    @Transactional(readOnly = true)
    public void syncPendingMatches() {
        // 대용량 모드가 아니면 스킵
        if (!syncManager.isBulkMode()) {
            return;
        }

        log.info("=== 대용량 모드 감지 - 대기 중인 데이터 동기화 시작 ===");

        long startTime = System.currentTimeMillis();
        int totalSynced = 0;

        try {
            // CREATE 타입 동기화
            totalSynced += syncPendingByType(MatchEsSyncEvent.SyncType.CREATE);

            // UPDATE 타입 동기화
            totalSynced += syncPendingByType(MatchEsSyncEvent.SyncType.UPDATE);

            // DELETE 타입 동기화
            totalSynced += syncPendingByType(MatchEsSyncEvent.SyncType.DELETE);

            long endTime = System.currentTimeMillis();
            log.info("=== 대기 데이터 동기화 완료 === 총 {}건, 소요 시간: {}ms",
                    totalSynced, (endTime - startTime));

            // 동기화 완료 후 대용량 모드 해제
            syncManager.deactivateBulkMode();

        } catch (Exception e) {
            log.error("대기 데이터 동기화 실패", e);
        }
    }

    /**
     * 특정 타입의 대기 중인 Match 동기화
     */
    private int syncPendingByType(MatchEsSyncEvent.SyncType syncType) {
        Set<Object> pendingIds = syncPublisher.getPendingSyncIds(syncType);

        if (pendingIds == null || pendingIds.isEmpty()) {
            return 0;
        }

        log.info("{} 타입 동기화 시작 - 대기 건수: {}", syncType, pendingIds.size());

        int synced = 0;
        for (Object obj : pendingIds) {
            try {
                Long matchId = Long.parseLong(obj.toString());

                if (syncType == MatchEsSyncEvent.SyncType.DELETE) {
                    matchSearchRepository.deleteById(String.valueOf(matchId));
                } else {
                    Match match = matchRepository.findById(matchId).orElse(null);
                    if (match != null) {
                        MatchDocument document = MatchDocument.from(match);
                        matchSearchRepository.save(document);
                    }
                }

                synced++;
            } catch (Exception e) {
                log.error("Match 동기화 실패 - matchId: {}", obj, e);
            }
        }

        // 동기화 완료 후 대기 목록 삭제
        syncPublisher.clearPendingSync(syncType);

        log.info("{} 타입 동기화 완료 - {}건", syncType, synced);
        return synced;
    }
}