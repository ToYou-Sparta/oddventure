package org.example.oddventure.domain.team.service;

import static org.example.oddventure.domain.team.exception.TeamErrorCode.TEAM_NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.team.dto.TeamResponse;
import org.example.oddventure.domain.team.entity.Team;
import org.example.oddventure.domain.team.exception.TeamException;
import org.example.oddventure.domain.team.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public Page<TeamResponse> getAllTeam(Pageable pageable) {
        Page<Team> team = teamRepository.findAll(pageable);
        return team.map(TeamResponse::of);
    }

    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new TeamException(TEAM_NOT_FOUND));
        return TeamResponse.of(team);
    }
}
