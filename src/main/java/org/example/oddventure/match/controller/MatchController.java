package org.example.oddventure.match.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.match.entity.Match;
import org.example.oddventure.match.enums.MatchStatus;
import org.example.oddventure.match.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("matches")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public List<Match> findAll() {
        return matchService.findAll();
    }

    @GetMapping("/{matchId}")
    public Match findById(@PathVariable Long matchId) {
        return matchService.findById(matchId);
    }

    @GetMapping("/search")
    public List<Match> searchMatches(
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) MatchStatus status
    ) {
        return matchService.searchMatches(teamName, status);
    }
}
