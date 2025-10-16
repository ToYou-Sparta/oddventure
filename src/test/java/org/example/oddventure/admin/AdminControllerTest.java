package org.example.oddventure.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.example.oddventure.domain.admin.controller.AdminController;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.service.AdminService;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("관리자 - 매치 생성 성공")
    void createMatch_Success() throws Exception {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        MatchCreateRequest request = new MatchCreateRequest("LCK", "T1", "Gen.G", startTime);
        MatchAdminResponse response = new MatchAdminResponse(1L, "LCK", "T1", "Gen.G", startTime,
                MatchStatus.SCHEDULED);
        given(adminService.createMatch(any(MatchCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teamA").value("T1"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("관리자 - 매치 생성 실패 - 유효성 검사 실패")
    void createMatch_Fail_InvalidInput() throws Exception {
        // given
        MatchCreateRequest request = new MatchCreateRequest("LCK", "", "Gen.G", LocalDateTime.now().plusDays(1));

        // when & then
        mockMvc.perform(post("/api/v1/admin/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("관리자 - 매치 정보 수정 성공")
    void updateMatch_Success() throws Exception {
        // given
        Long matchId = 1L;
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2).withNano(0);
        MatchUpdateRequest request = new MatchUpdateRequest(
                "New MatchName", "New Team A", "New Team B", newStartTime, MatchStatus.ONGOING
        );
        MatchAdminResponse response = new MatchAdminResponse(
                matchId, "New MatchName", "New Team A", "New Team B", newStartTime, MatchStatus.ONGOING
        );

        given(adminService.updateMatch(any(Long.class), any(MatchUpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/matches/{matchId}", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchId").value(matchId))
                .andExpect(jsonPath("$.data.teamA").value("New Team A"))
                .andExpect(jsonPath("$.data.teamB").value("New Team B"))
                .andExpect(
                        jsonPath("$.data.startTime").value(newStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.data.status").value("ONGOING"));
    }
}