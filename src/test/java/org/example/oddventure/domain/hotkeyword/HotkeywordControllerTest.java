package org.example.oddventure.domain.hotkeyword;

import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.hotKeywords.controller.HotKeywordsController;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import java.util.Set;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotKeywordsController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
public class HotkeywordControllerTest extends RestDocsTestSupport{

    @MockitoBean
    private HotKeywordsService hotKeywordsService;

    @Test
    @DisplayName("인기 검색어 Top5를 조회할 수 있다.")
    void getTop5HotKeywords_success() throws Exception {
        // given
        Set<Object> top5 = Set.of(
                "korea",
                "america",
                "china",
                "india",
                "england",
                "japan"
        );

        HotKeywordsResponse response = HotKeywordsResponse.of(top5);
        when(hotKeywordsService.getHotKeywords()).thenReturn(response);

        // when & then
        ResultActions result = mockMvc.perform(get("/api/v1/hotkeyword"));


        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.hotKeywords").description("인기검색어 top5"))));
    }
}
