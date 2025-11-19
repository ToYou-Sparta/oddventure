package org.example.oddventure.common.config;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.user.service.UserService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PointJobConfig {

    private static final int CHUNK_SIZE = 500;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final UserService userService;

    @Bean
    public Job pointSetJob(Step pointSetStep) {
        return new JobBuilder("pointSetJob", jobRepository)
                .start(pointSetStep)
                .build();
    }

    @Bean
    public Step pointSetStep(JpaPagingItemReader<Bet> pointSetReader) {
        return new StepBuilder("pointSetStep", jobRepository)
                .<Bet, Bet>chunk(CHUNK_SIZE, transactionManager)
                .reader(pointSetReader)
                .processor(pointSetProcessor())
                .writer(pointSetWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Bet> pointSetReader(@Value("#{jobParameters['matchIds']}") String matchIdsParam) {
        List<Long> matchIds = Arrays.stream(matchIdsParam.split(","))
                .map(Long::valueOf)
                .toList();

        return new JpaPagingItemReaderBuilder<Bet>()
                .name("pointSetReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString(
                        "SELECT b FROM Bet b " +
                                "JOIN FETCH b.user " +
                                "JOIN FETCH b.match " +
                                "WHERE b.match.id IN :matchIds " +
                                "AND b.deleted = false " +
                                "ORDER BY b.id")
                .parameterValues(Map.of("matchIds", matchIds))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Bet, Bet> pointSetProcessor() {
        return bet -> {
            Match match = bet.getMatch();
            SelectedTeam winner = match.getWinner().equals(match.getTeamA())
                    ? SelectedTeam.Team_A
                    : SelectedTeam.Team_B;

            if (!bet.getSelectedTeam().equals(winner)) {
                return bet;
            }

            bet.setWin(true);
            BigDecimal point = bet.getBetAmount().multiply(bet.getOddsAtBetting());
            PointAdjustRequest request = PointAdjustRequest.of(point, "배당금 지급");
            userService.adjustUserPoints(bet.getUser().getId(), request);

            return bet;
        };
    }

    @Bean
    public JpaItemWriter<Bet> pointSetWriter() {
        JpaItemWriter<Bet> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}