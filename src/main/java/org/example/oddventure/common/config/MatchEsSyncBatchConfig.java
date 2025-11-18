package org.example.oddventure.common.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.match.document.MatchDocument;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.elasticsearch.MatchSearchRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * MySQL → Elasticsearch 동기화 Spring Batch 설정
 *
 * <p>Chunk 기반 처리로 안정적인 대용량 데이터 동기화 제공:
 * <ul>
 *   <li>자동 재시작: 실패 지점부터 재시작 가능</li>
 *   <li>트랜잭션 관리: Chunk 단위 커밋/롤백</li>
 *   <li>실행 이력: JobRepository에 모든 실행 이력 저장</li>
 *   <li>병렬 처리: Multi-thread Step 지원 가능</li>
 * </ul>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MatchEsSyncBatchConfig {

    private static final int CHUNK_SIZE = 1000;  // Chunk 크기
    private static final String JOB_NAME = "matchEsSyncJob";
    private static final String STEP_NAME = "matchEsSyncStep";
    private static final String READER_NAME = "matchEsSyncReader";

    private final JobRepository jobRepository; // Spring Batch 실행 이력 저장소
    private final PlatformTransactionManager transactionManager; //Chunk 단위로 Commit/Rollback 처리
    private final EntityManagerFactory entityManagerFactory; //Reader가 JPA 데이터 조회시 사용
    private final MatchSearchRepository matchSearchRepository;

    /**
     * MySQL → Elasticsearch 전체 동기화 Job
     */
    @Bean
    public Job matchEsSyncJob(Step matchEsSyncStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(matchEsSyncStep)
                .build();
    }

    /**
     * Match 동기화 Step (Chunk 기반)
     *
     * <p>처리 흐름:
     * 1. Reader: MySQL에서 Match 페이징 조회
     * 2. Processor: Match → MatchDocument 변환
     * 3. Writer: Elasticsearch Bulk 저장
     */
    @Bean
    public Step matchEsSyncStep(JpaPagingItemReader<Match> matchEsSyncReader) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<Match, MatchDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(matchEsSyncReader)
                .processor(matchEsSyncProcessor())
                .writer(matchEsSyncWriter())
                .build();
    }

    /**
     * MySQL Match 읽기 (JPA Paging)
     *
     * <p>삭제되지 않은 모든 Match를 페이징으로 조회
     */
    @Bean
    public JpaPagingItemReader<Match> matchEsSyncReader() {
        return new JpaPagingItemReaderBuilder<Match>()
                .name(READER_NAME)
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT m FROM Match m " +
                        "WHERE m.deleted = false " +
                        "ORDER BY m.id"
                )
                .pageSize(CHUNK_SIZE)
                .build();
    }

    /**
     * Match → MatchDocument 변환 Processor
     */
    @Bean
    public ItemProcessor<Match, MatchDocument> matchEsSyncProcessor() {
        return match -> {
            try {
                return MatchDocument.from(match);
            } catch (Exception e) {
                log.error("Match → MatchDocument 변환 실패 - matchId: {}", match.getId(), e);
                // 변환 실패 시 null 반환 (해당 항목은 Writer로 전달되지 않음)
                return null;
            }
        };
    }

    /**
     * Elasticsearch Bulk 저장 Writer
     *
     * <p>Chunk 단위로 모아서 Elasticsearch에 Bulk 저장
     */
    @Bean
    public ItemWriter<MatchDocument> matchEsSyncWriter() {
        return chunk -> {
            List<? extends MatchDocument> documents = chunk.getItems();
            if (documents.isEmpty()) {
                return;
            }

            try {
                matchSearchRepository.saveAll(documents);
                log.info("Elasticsearch Bulk 저장 완료 - {}건", documents.size());
            } catch (Exception e) {
                log.error("Elasticsearch Bulk 저장 실패 - {}건", documents.size(), e);
                throw e;  // 실패 시 Chunk 롤백
            }
        };
    }
}