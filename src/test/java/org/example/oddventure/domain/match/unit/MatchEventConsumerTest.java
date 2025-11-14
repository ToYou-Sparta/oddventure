package org.example.oddventure.domain.match.unit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.example.oddventure.domain.match.dto.event.MatchStartEventDto;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.event.MatchEventConsumer;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MatchEventConsumerTest {

    @InjectMocks
    private MatchEventConsumer matchEventConsumer;

    @Mock
    private MatchService matchService;

    @Test
    @DisplayName("매치 스타트 이벤트를 소비하여 매치 상태값 변경에 성공한다.")
    public void consumeMatchStartEvent_success() {
        //given
        Long fetchId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        MatchStartEventDto dto = MatchStartEventDto.builder()
                .fetchId(fetchId)
                .startTime(startTime)
                .build();

        doNothing().when(matchService).updateStatus(anyLong(), eq(MatchStatus.ONGOING));

        //when
        matchEventConsumer.consumeMatchStartEvent(dto);

        //then
        verify(matchService).updateStatus(fetchId, MatchStatus.ONGOING);
    }
}
