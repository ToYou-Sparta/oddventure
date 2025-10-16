package org.example.oddventure.domain.team.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.reponse.ApiPageResponse;
import org.example.oddventure.common.dto.reponse.ApiResponse;
import org.example.oddventure.domain.team.dto.TeamResponse;
import org.example.oddventure.domain.team.service.TeamService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiPageResponse<TeamResponse>> findAllTeam(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10")int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiPageResponse.success(teamService.findAllTeam(pageable));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> findTeamById(@PathVariable Long teamId) {
        return ApiResponse.success(teamService.findTeamById(teamId));
    }
}
