package org.example.oddventure.match.service;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.match.entity.Match;
import org.example.oddventure.match.enums.MatchStatus;
import org.example.oddventure.match.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public List<Match> findAll() {
        return matchRepository.findAll();
    }

    public Match findById(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 경기 일정을 찾을 수 없습니다."));
    }

    public List<Match> searchMatches(String teamName, MatchStatus status) {
        return matchRepository.findByConditions(teamName, status);
    }
}
