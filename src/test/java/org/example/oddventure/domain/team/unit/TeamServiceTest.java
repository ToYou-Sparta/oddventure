package org.example.oddventure.domain.team.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.example.oddventure.domain.team.dto.TeamResponse;
import org.example.oddventure.domain.team.entity.Team;
import org.example.oddventure.domain.team.repository.TeamRepository;
import org.example.oddventure.domain.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    Team team1;
    Team team2;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks // 테스트 대상
    private TeamService teamService;

    @BeforeEach
    public void setup() {
        team1 = Team.builder().name("T1").build();
        team2 = Team.builder().name("GEN.G").build();
    }

    @Test
    @DisplayName("전체 팀 조회 시 모든 팀 정보를 반환한다")
    void shouldReturnAllTeams() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Team> teams = new PageImpl<>(List.of(team1, team2));
        when(teamRepository.findAll(pageable)).thenReturn(teams);

        // when
        Page<TeamResponse> result = teamService.getAllTeam(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("T1");
        assertThat(result.getContent().get(1).name()).isEqualTo("GEN.G");
    }

    @Test
    @DisplayName("ID로 팀을 조회하면 해당 팀 정보를 반환한다")
    void shouldReturnTeamById() {
        // given
        Long id = 1L;
        when(teamRepository.findById(id)).thenReturn(Optional.ofNullable(team1));

        // when
        TeamResponse result = teamService.getTeamById(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("T1");
    }
}