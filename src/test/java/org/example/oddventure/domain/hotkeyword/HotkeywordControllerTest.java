package org.example.oddventure.hotkeyword;

import org.example.oddventure.base.RedisTestContainerConfig;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.bet.controller.BetController;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.hotKeywords.controller.HotKeywordsController;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.entity.HotKeywords;
import org.example.oddventure.domain.hotKeywords.repository.HotKeywordsRepository;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotKeywordsController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
@ExtendWith(RestDocumentationExtension.class)
public class HotkeywordControllerTest extends RedisTestContainerConfig {

    private RestDocumentationResultHandler restDocs;

    private MockMvc mockMvc;

    private Set<Object> top5;

    @MockitoBean
    private HotKeywordsService hotKeywordsService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        top5 = Set.of(
                "korea", 6.0,
                "america", 5.0,
                "china", 4.0,
                "india", 3.0,
                "england", 2.0,
                "japan", 1.0
        );

        this.restDocs = MockMvcRestDocumentation.document("{class-name}/{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(restDocs) // 디폴트 설정
                .build();
    }

    @Test
    public void 인기_검색어_Top5를_조회할_수_있다() throws Exception {
        // given
        HotKeywordsResponse response = HotKeywordsResponse.of(top5);
        when(hotKeywordsService.getHotKeywords()).thenReturn(response);


        // when & then
        mockMvc.perform(get("/api/v1/hotkeyword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("httpStatus").description("HTTP 상태 코드"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("응답 데이터"),
                                fieldWithPath("data.hotKeywords").description("인기검색어 top5"),
                                fieldWithPath("timestamp").description("응답 시간"))
                ));
    }
}
