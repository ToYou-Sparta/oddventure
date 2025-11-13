package org.example.oddventure.domain.ai.unit.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;
import org.example.oddventure.domain.ai.tools.HotKeywordTools;
import org.example.oddventure.domain.hotKeywords.dto.HotKeywordsResponse;
import org.example.oddventure.domain.hotKeywords.service.HotKeywordsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HotKeywordToolsTest {

    @Mock
    private HotKeywordsService hotKeywordsService;

    @InjectMocks
    private HotKeywordTools hotKeywordTools;

    @Test
    @DisplayName("getHotKeywords를 호출하고 인기 검색어를 반환한다")
    void queryHotKeywords_success() {
        // given
        Set<Object> hotKeywords = new LinkedHashSet<>(Set.of("T1", "FaZe Clan", "Team Vitality"));
        HotKeywordsResponse mockResponse = HotKeywordsResponse.of(hotKeywords);

        when(hotKeywordsService.getHotKeywords()).thenReturn(mockResponse);

        // when
        HotKeywordsResponse response = hotKeywordTools.queryHotKeywords();

        // then
        assertThat(response).isNotNull();
        Set<Object> expected = Set.of("FaZe Clan", "Team Vitality", "T1");
        assertThat(response.hotKeywords()).isEqualTo(expected);
    }
}
