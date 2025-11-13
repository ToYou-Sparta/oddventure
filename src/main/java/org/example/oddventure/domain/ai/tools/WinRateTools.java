package org.example.oddventure.domain.ai.tools;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.dto.WinRateItem;
import org.example.oddventure.domain.ai.dto.WinRateResponse;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WinRateTools {

    private final MatchRepository matchRepository;

    @Tool(
            name = "analyze_winning_rate",
            description = "팀별 승/패 집계를 기반으로 승률(%)을 계산한다." +
                    "teamA/teamB중 하나 또는 둘 다 전달 가능. 둘 다 있으면 비교 모드로 동작한다."
    )
    public WinRateResponse analyzeWinningRate(
            @ToolParam(description = "첫 번째 팀 이름", required = false) String teamA,
            @ToolParam(description = "두 번째 팀 이름(선택)", required = false) String teamB
    ) {
        List<String> winMatches = matchRepository.findByWinnerIsNotNull();
        List<String> loseMatches = matchRepository.findByLoserIsNotNull();

        Map<String, Long> winCount = winMatches.stream()
                .collect(Collectors.groupingBy((String winner) -> winner, Collectors.counting()));

        Map<String, Long> loseCount = loseMatches.stream()
                .collect(Collectors.groupingBy((String loser) -> loser, Collectors.counting()));

        Function<String, WinRateItem> toItem = team -> {
            Long w = winCount.getOrDefault(team, 0L);
            Long l = loseCount.getOrDefault(team, 0L);
            return WinRateItem.of(team, w, l);
        };

        String a = (teamA == null || teamA.isBlank()) ? null : teamA.trim();
        String b = (teamB == null || teamB.isBlank()) ? null : teamB.trim();

        if (a == null && b == null) {
            return WinRateResponse.teamNameIsNull("팀명을 알려주세요.");
        }

        // 비교
        if (a != null && b != null) {
            WinRateItem itemA = toItem.apply(a);
            WinRateItem itemB = toItem.apply(b);

            String content = String.format(
                    "%s 승률 %d%%(승 %d · 패 %d), %s 승률 %d%%(승 %d · 패 %d). " +
                            "표본 수에 따라 불확실성이 있을 수 있습니다.",
                    itemA.team(), itemA.winningRate(), itemA.wins(), itemA.losses(),
                    itemB.team(), itemB.winningRate(), itemB.wins(), itemB.losses()
            );
            return WinRateResponse.ok(List.of(itemA, itemB), a + " vs " + b, content);
        }

        // 단일 팀
        String t = (a != null) ? a : b;
        WinRateItem item = toItem.apply(t);
        String content = String.format(
                "%s의 누적 승률은 %d%%(승 %d · 패 %d)입니다. " +
                        "최근 폼/상대전적/라인업 등 추가 요소는 별도 데이터 연동 시 반영 가능합니다.",
                item.team(), item.winningRate(), item.wins(), item.losses()
        );
        return WinRateResponse.ok(List.of(item), null, content);
    }
}
