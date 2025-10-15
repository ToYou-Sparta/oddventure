package org.example.oddventure.match.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.domain.match.controller.MatchController;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.enums.MatchWinner;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MatchController.class)
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @Test
    @DisplayName("GET /matches - 경기 목록 조회 성공")
    void getMatches() throws Exception {

        // given
        MatchResponse response = new MatchResponse(
                1L,
                "T1",
                "GEN.G",
                new BigDecimal("10000"),
                new BigDecimal("8000"),
                LocalDateTime.of(2025, 10, 16, 18, 0),
                LocalDateTime.of(2025, 10, 16, 20, 0),
                MatchStatus.SCHEDULED,
                MatchWinner.NO_MATCH,
                LocalDateTime.of(2025, 10, 10, 20, 0)
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("startTime").ascending());
        Page<MatchResponse> responsePage = new PageImpl<>(List.of(response), pageable, 1);
        when(matchService.getMatches(any())).thenReturn(responsePage);

        // when & then
        mockMvc.perform(get("/api/v1/matches")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].matchId").value(1))
                .andExpect(jsonPath("$.data[0].teamB").value("GEN.G"))
                .andExpect(jsonPath("$.data[0].status").value("SCHEDULED")
                );
    }
}
