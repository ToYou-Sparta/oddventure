package org.example.oddventure.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.reponse.ApiPageResponse;
import org.example.oddventure.common.dto.reponse.ApiResponse;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<ApiPageResponse<MatchResponse>> getMatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());

        Page<MatchResponse> matches = matchService.getMatches(pageable);

        return ApiPageResponse.success(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatch(
            @PathVariable Long matchId
    ) {

        return ApiResponse.success(matchService.getMatch(matchId));
    }

//    @GetMapping("/search")
//    public List<Match> searchMatches(
//            @RequestParam(required = false) String teamName,
//            @RequestParam(required = false) MatchStatus status
//    ) {
//        return matchService.searchMatches(teamName, status);
//    }
}
