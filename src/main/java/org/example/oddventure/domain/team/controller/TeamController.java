package org.example.oddventure.domain.team.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiPageResponse;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.team.dto.TeamResponse;
import org.example.oddventure.domain.team.service.TeamService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiPageResponse<TeamResponse>> getAllTeam(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiPageResponse.success(teamService.getAllTeam(pageable), "팀 목록 조회에 성공했습니다.");
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long teamId) {
        return ApiResponse.success(teamService.getTeamById(teamId), "팀 상세 조회에 성공했습니다.");
    }
}