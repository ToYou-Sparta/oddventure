package org.example.oddventure.match.unit;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.common.config.JpaAuditingConfig;
import org.example.oddventure.common.config.QueryDslConfig;
import org.example.oddventure.domain.match.dto.projection.MatchProjection;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
public class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("조회수 증가 성공")
    void incrementViewCount() {

        // given
        Match match = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        matchRepository.save(match);

        // when
        matchRepository.incrementViewCount(match.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        Match updatedMatch = matchRepository.findById(match.getId()).orElseThrow();
        assertThat(updatedMatch.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("조건 검색 성공")
    void searchByCondition() {

        // given
        Match match1 = Match.builder()
                .matchName("LCK")
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.of(2025, 10, 15, 0, 0))
                .build();
        Match match2 = Match.builder()
                .matchName("LCK")
                .teamA("KT")
                .teamB("DRX")
                .startTime(LocalDateTime.of(2025, 10, 21, 0, 0))
                .build();
        matchRepository.saveAll(List.of(match1, match2));

        MatchSearchCondition condition = new MatchSearchCondition(
                "LCK",
                LocalDateTime.of(2025, 10, 10, 0, 0),
                LocalDateTime.of(2025, 10, 20, 0, 0)
        );

        // when
        Page<MatchProjection> result = matchRepository.searchByCondition(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).teamB()).isEqualTo("GEN.G");
    }

    @Test
    @DisplayName("정렬 순서 검증 - 경기 시작 시간 가까운순, 경기 이름 가나다순")
    void searchByCondition_orderBy() {

        // given
        Match match1 = Match.builder()
                .matchName("CCC")
                .teamA("TeamA")
                .teamB("TeamB")
                .startTime(LocalDateTime.of(2025, 10, 20, 0, 0))
                .build();
        Match match2 = Match.builder()
                .matchName("BBB")
                .teamA("TeamA")
                .teamB("TeamB")
                .startTime(LocalDateTime.of(2025, 10, 19, 0, 0))
                .build();
        Match match3 = Match.builder()
                .matchName("AAA")
                .teamA("TeamA")
                .teamB("TeamB")
                .startTime(LocalDateTime.of(2025, 10, 20, 0, 0))
                .build();
        matchRepository.saveAll(List.of(match1, match2, match3));

        MatchSearchCondition condition = new MatchSearchCondition("", null, null);

        // when
        Page<MatchProjection> result = matchRepository.searchByCondition(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).matchName()).isEqualTo("BBB");
        assertThat(result.getContent().get(1).matchName()).isEqualTo("AAA");
    }
}
