package org.example.oddventure.domain.match.event;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OddsNotificationPolicy {

    /**
     * 경기 시작 2시간 전부터 알림 허용
     * <p>
     * [간격 규칙] - 2시간~30분 전 → 10분 - 30분~10분 전 → 5분 - 10분 전~시작 전 → 1분
     *
     * @param matchStart   경기 시작 시간
     * @param lastSentTime 마지막 알림 발송 시간 (첫 발송 시 null)
     * @param now          현재 시간
     */
    public boolean shouldSend(LocalDateTime matchStart,
                              LocalDateTime lastSentTime,
                              LocalDateTime now) {

        // (1) 알림 허용 구간 검사 (2시간 전 ~ 경기 시작 직전)
        if (now.isBefore(matchStart.minusHours(2)) || now.isAfter(matchStart)) {
            return false;
        }

        // (2) 시간 차이로 requiredInterval 계산
        Duration untilStart = Duration.between(now, matchStart);
        long minutes = untilStart.toMinutes();

        Duration requiredInterval =
                minutes > 30 ? Duration.ofMinutes(10) :
                        minutes > 10 ? Duration.ofMinutes(5) :
                                Duration.ofMinutes(1);

        // (3) 첫 알림이면 바로 전송
        if (lastSentTime == null) {
            return true;
        }

        // (4) 마지막 알림 이후 requiredInterval만큼 지났는지 체크
        return Duration.between(lastSentTime, now).compareTo(requiredInterval) >= 0;
    }
}
