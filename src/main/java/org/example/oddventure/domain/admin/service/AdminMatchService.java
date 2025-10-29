package org.example.oddventure.domain.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMatchService {

    private final GridService gridService;
    private final MatchService matchService;

    // 매치 일정 연동
    @Transactional
    public void createMatchSchedules() {
        List<MatchScheduleDto> fetchResponses = gridService.fetchMatchSchedules();

        fetchResponses.forEach(matchService::fetchMatches);
    }

    // 매치 결과 연동
    @Transactional
    public void createMatchResult(Long fetchId) {
        MatchResultDto dto = gridService.fetchMatchResult(fetchId);

        matchService.updateMatchResult(fetchId, dto.winner(), dto.looser());
    }
}
