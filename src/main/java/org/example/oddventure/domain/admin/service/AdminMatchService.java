package org.example.oddventure.domain.admin.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.admin.dto.response.MatchCreateAdminResponse;
import org.example.oddventure.domain.grid.dto.MatchResultDto;
import org.example.oddventure.domain.grid.dto.MatchScheduleDto;
import org.example.oddventure.domain.grid.service.GridService;
import org.example.oddventure.domain.match.dto.MatchCreateDto;
import org.example.oddventure.domain.match.exception.MatchException;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMatchService {

    private final GridService gridService;
    private final MatchService matchService;

    // 매치 생성
    @Transactional
    public MatchCreateAdminResponse createMatch() {
        List<MatchScheduleDto> fetchResponses = gridService.fetchMatchSchedules();
        List<Long> fetchIds = new ArrayList<>();

        for (MatchScheduleDto response : fetchResponses) {
            try {
                MatchCreateDto matchCreateDto = matchService.createMatch(response);
                fetchIds.add(matchCreateDto.fetchId());
            } catch (MatchException e) {
                log.info("fetchId={}: {}", response.fetchId(), e.getMessage());
            }
        }

        return MatchCreateAdminResponse.of(fetchIds);
    }

    // 매치 결과 연동
    @Transactional
    public void createMatchResult(Long fetchId) {
        MatchResultDto dto = gridService.fetchMatchResult(fetchId);

        matchService.updateMatchResult(fetchId, dto.winner(), dto.loser());
    }
}
