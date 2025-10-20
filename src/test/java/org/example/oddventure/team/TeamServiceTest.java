package org.example.oddventure.team;

import org.assertj.core.api.Assertions;
import org.example.oddventure.domain.team.dto.TeamResponse;
import org.example.oddventure.domain.team.entity.Team;
import org.example.oddventure.domain.team.repository.TeamRepository;
import org.example.oddventure.domain.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks // 테스트 대상
    private TeamService teamService;

    Team team1;
    Team team2;

    @BeforeEach
    public void setup() {
        team1 = Team.builder()
                .name("T1")
                .build();

        team2 = Team.builder()
                .name("GEN.G")
                .build();
    }

    @Test
    void 전체_Team을_조회할_수_있다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Team> teams = new PageImpl<>(List.of(team1, team2));
        when(teamRepository.findAll(pageable)).thenReturn(teams);

        // when
        Page<TeamResponse> result = teamService.findAllTeam(pageable);

        // then
        assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getContent().get(0).getName()).isEqualTo("T1");
        Assertions.assertThat(result.getContent().get(1).getName()).isEqualTo("GEN.G");
    }

    @Test
    void Team을_id로_조회할_수_있다() {
        // given
        Long id = 1L;

        // when
        TeamResponse result = teamService.findTeamById(id);

        // then
        assertThat(result).isNotNull();
        Assertions.assertThat(result.getName()).isEqualTo("T1");
    }
}
