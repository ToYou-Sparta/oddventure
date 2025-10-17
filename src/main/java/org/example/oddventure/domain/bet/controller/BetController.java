package org.example.oddventure.domain.bet.controller;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.dto.response.ApiPageResponse;
import org.example.oddventure.common.dto.response.ApiResponse;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.service.BetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bets")
public class BetController {

    private final BetService betService;

    @PostMapping
    public ResponseEntity<ApiResponse<BetCreateResponse>> createBet(Long userId,
                                                                    @RequestBody BetCreateRequest betCreateRequest) {
        BetCreateResponse betCreateResponse = betService.createBet(userId, betCreateRequest);
        return ApiResponse.created(betCreateResponse, "베팅이 완료 되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiPageResponse<BetResponse>> getBet(Long userId,
                                                               @RequestParam(defaultValue = "0") int number,
                                                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(number, size);
        Page<BetResponse> betResponsePage = betService.getBet(userId, pageable);
        return ApiPageResponse.success(betResponsePage, "베팅 내역이 조회되었습니다.");
    }
}
