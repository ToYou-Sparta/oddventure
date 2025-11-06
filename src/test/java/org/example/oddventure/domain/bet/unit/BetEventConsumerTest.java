package org.example.oddventure.domain.bet.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.bet.dto.PointEventDto;
import org.example.oddventure.domain.bet.event.BetEventConsumer;
import org.example.oddventure.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BetEventConsumerTest {

    @InjectMocks
    private BetEventConsumer betEventConsumer;

    @Mock
    private UserService userService;


    @Test
    @DisplayName("포인트 이벤트를 소비하여 포인트를 지급에 성공한다.")
    public void consumeBetEvent_success() {
        //given
        Long userId = 1L;
        BigDecimal betAmount = BigDecimal.valueOf(1000);
        PointEventDto dto = PointEventDto.from(userId, betAmount);
        PointAdjustRequest request = PointAdjustRequest.of(dto.betAmount(), "배당금 지급");
        PointAdjustResponse response = new PointAdjustResponse(userId, "test", dto.betAmount(),
                BigDecimal.valueOf(1000));

        when(userService.adjustUserPoints(anyLong(), any(PointAdjustRequest.class))).thenReturn(response);

        //when
        betEventConsumer.consumePointEvent(dto);

        //then
        verify(userService).adjustUserPoints(eq(userId), eq(request));
    }

}
