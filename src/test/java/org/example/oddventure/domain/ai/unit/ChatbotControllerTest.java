package org.example.oddventure.domain.ai.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.ai.controller.ChatController;
import org.example.oddventure.domain.ai.controller.ChatController.UserMessage;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
@ActiveProfiles({"local", "test"})
public class ChatbotControllerTest extends RestDocsTestSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatbotService chatbotService;

    @Test
    @DisplayName("AI 챗봇이 자연어 텍스트 응답을 반환한다")
    void reply_success() throws Exception {
        // given
        UserMessage userMessage = new UserMessage("FaZe Clan 경기 일정 알려줘");
        String aiResponse = "오늘은 FaZe Clan과 Team Vitality의 경기가 있습니다.";

        when(chatbotService.reply(1L, userMessage.message())).thenReturn(aiResponse);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userMessage)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(aiResponse))
                .andDo(restDocs.document(RestDocsUtils.successWithDataFields()));
    }
}
