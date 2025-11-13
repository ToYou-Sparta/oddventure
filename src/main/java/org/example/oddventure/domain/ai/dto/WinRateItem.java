package org.example.oddventure.domain.ai.dto;

public record WinRateItem(
        String team,
        Long wins,
        Long losses,
        int winningRate
) {
    public static WinRateItem of(String team, Long wins, Long losses) {
        Long total = Math.max(0, wins + losses);
        int rate = (total == 0) ? 0 : Math.toIntExact(Math.round((wins * 100.0) / total));
        return new WinRateItem(team, wins, losses, rate);
    }
}
