package org.example.oddventure.common.config;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.grid.service.GridService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final GridService gridService;

    @Bean
    public Job matchScheduleJob() {
        return new JobBuilder("matchScheduleJob", jobRepository)
                .start(matchScheduleStep())
                .build();
    }

    @Bean
    public Step matchScheduleStep() {
        return new StepBuilder("matchScheduleStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    gridService.fetchMatchSchedules();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
