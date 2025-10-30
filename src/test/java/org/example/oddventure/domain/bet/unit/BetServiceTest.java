package org.example.oddventure.domain.bet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.event.RedisPublisher;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BetServiceTest {

    @InjectMocks
    private BetService betService;

    @Mock
    private BetRepository betRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisPublisher redisPublisher;

    @Test
    @DisplayName("베팅을 생성 성공하면 유저 포인트가 차감되고 베팅 금액이 저장된다.")
    void createBet_success() {
        //given
        Long userId = 1L;
        User user = User.builder()
                .username("test")
                .email("test1234@test.com")
                .password("test1234!")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Long matchId = 1L;
        Match match = Match.builder()
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(match, "id", matchId);

        match.plusTeamA(new BigDecimal("6000"));
        match.plusTeamB(new BigDecimal("4000"));

        BetCreateRequest request = new BetCreateRequest(1L, SelectedTeam.Team_A, 1000L);

        Long betId = 1L;
        Bet bet = Bet.builder()
                .user(user)
                .match(match)
                .selectedTeam(SelectedTeam.Team_A)
                .betAmount(new BigDecimal("1000"))
                .oddsAtBetting(new BigDecimal("2"))
                .build();
        ReflectionTestUtils.setField(bet, "id", betId);

        given(betRepository.save(any(Bet.class))).willReturn(bet);
        given(userRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(user));
        given(matchRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(match));

        //when
        BetCreateResponse response = betService.createBet(user.getId(), request);

        //then
        assertThat(response.selectedTeam()).isEqualTo(SelectedTeam.Team_A);
        assertThat(response.selectedTeamName()).isEqualTo("T1");
        assertThat(response.betAmount()).isEqualTo(new BigDecimal("1000"));
        assertThat(response.oddsAtBetting()).isEqualTo(new BigDecimal("1.50"));
        assertThat(response.userPointAfter()).isEqualTo(new BigDecimal("0"));
        verify(betRepository).save(any(Bet.class));
    }

    @Test
    @DisplayName("베팅 삭제에 성공하면 포인트가 환불되고 베팅 금액이 감소된다.")
    void deleteBet_success() {
        //given
        Long userId = 1L;
        User user = User.builder()
                .username("test")
                .email("test1234@test.com")
                .password("test1234!")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Long matchId = 1L;
        Match match = Match.builder()
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(match, "id", matchId);

        match.plusTeamA(new BigDecimal("6000"));
        match.plusTeamB(new BigDecimal("4000"));

        Long betId = 1L;
        Bet bet = Bet.builder()
                .user(user)
                .match(match)
                .selectedTeam(SelectedTeam.Team_A)
                .betAmount(new BigDecimal("1000"))
                .oddsAtBetting(new BigDecimal("2"))
                .build();
        ReflectionTestUtils.setField(bet, "id", betId);

        match.plusTeamA(bet.getBetAmount());

        given(betRepository.findByIdForDelete(anyLong())).willReturn(Optional.of(bet));
        given(matchRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(match));
        given(userRepository.findByIdForUpdate(anyLong())).willReturn(Optional.of(user));

        //when
        BetDeleteResponse response = betService.deleteBet(user.getId(), betId);

        //then
        assertThat(bet.isDeleted()).isTrue();
        assertThat(response.refundAmount()).isEqualTo(new BigDecimal("1000"));
        assertThat(response.userPointAfter()).isEqualTo(new BigDecimal("2000"));
        assertThat(match.getTotalAmountA()).isEqualTo(new BigDecimal("6000"));
    }

    @Test
    @DisplayName("베팅 내역 조회에 성공한다.")
    void getBets_success() {
        //given
        Long userId = 1L;
        User user = User.builder()
                .username("test")
                .email("test1234@test.com")
                .password("test1234!")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Long matchId = 1L;
        Match match = Match.builder()
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(match, "id", matchId);

        match.plusTeamA(new BigDecimal("6000"));
        match.plusTeamB(new BigDecimal("4000"));

        Long betId = 1L;
        Bet bet = Bet.builder()
                .user(user)
                .match(match)
                .selectedTeam(SelectedTeam.Team_A)
                .betAmount(new BigDecimal("1000"))
                .oddsAtBetting(new BigDecimal("2"))
                .build();
        ReflectionTestUtils.setField(bet, "id", betId);

        Pageable pageable = PageRequest.of(0, 1);
        Page<Bet> bets = new PageImpl<>(List.of(bet));

        given(betRepository.findByUserId(userId, pageable)).willReturn(bets);

        //when
        Page<BetResponse> responses = betService.getBets(userId, pageable);

        //then
        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(1);
        assertThat(responses.getContent()).hasSize(1);
        BetResponse response = responses.getContent().get(0);
        assertAll(
                () -> assertThat(response.betId()).isEqualTo(betId),
                () -> assertThat(response.selectedTeam()).isEqualTo(SelectedTeam.Team_A),
                () -> assertThat(response.betAmount()).isEqualTo(new BigDecimal("1000")),
                () -> assertThat(response.oddsAtBetting()).isEqualTo(new BigDecimal("2")),
                () -> assertThat(response.matchBetResponse().teamA()).isEqualTo("T1"),
                () -> assertThat(response.matchBetResponse().teamB()).isEqualTo("GEN.G")
        );
        verify(betRepository).findByUserId(userId, pageable);
    }
}