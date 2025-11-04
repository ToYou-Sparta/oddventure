package org.example.oddventure.domain.bet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.bet.service.BetTransactionService;
import org.example.oddventure.domain.event.RedisPublisher;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    private RedisPublisher redisPublisher;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private BetTransactionService betTransactionService;

    @Mock
    private RLock multiLock;

    @Mock
    private RLock userLock;

    @Mock
    private RLock matchLock;

    @Test
    @DisplayName("베팅을 생성 성공 (분산 락)")
    void createBet_success() throws InterruptedException {
        //given
        Long userId = 1L;
        Long matchId = 1L;
        Long betId = 1L;

        User user = User.builder().build(); // 트랜잭션 서비스가 반환할 객체
        Match match = Match.builder().teamA("T1").build(); // 트랜잭션 서비스가 반환할 객체
        ReflectionTestUtils.setField(match, "id", matchId);

        BetCreateRequest request = new BetCreateRequest(matchId, SelectedTeam.Team_A, 1000L);

        // 트랜잭션 서비스가 반환할 데이터 Mocking
        Bet bet = Bet.builder().user(user).match(match).selectedTeam(SelectedTeam.Team_A)
                .betAmount(new BigDecimal(1000)).oddsAtBetting(new BigDecimal("1.50")).build();
        ReflectionTestUtils.setField(bet, "id", betId);

        BetTransactionService.CreateBetData createBetData = new BetTransactionService.CreateBetData(
                bet, "T1", new BigDecimal("0"), new BigDecimal("1.50")
        );

        // Redisson MultiLock Mocking
        given(redissonClient.getLock("LOCK:USER_POINT:" + userId)).willReturn(userLock);
        given(redissonClient.getLock("LOCK:MATCH:" + matchId)).willReturn(matchLock);
        given(redissonClient.getMultiLock(userLock, matchLock)).willReturn(multiLock);
        given(multiLock.tryLock(10, 5, TimeUnit.SECONDS)).willReturn(true); // 락 획득 성공

        // 트랜잭션 서비스 호출 Mocking
        given(betTransactionService.createBetInternal(userId, request)).willReturn(createBetData);

        //when
        BetCreateResponse response = betService.createBet(userId, request);

        //then
        assertThat(response.selectedTeam()).isEqualTo(SelectedTeam.Team_A);
        assertThat(response.selectedTeamName()).isEqualTo("T1");
        assertThat(response.betAmount()).isEqualTo(new BigDecimal("1000"));
        assertThat(response.oddsAtBetting()).isEqualTo(new BigDecimal("1.50"));
        assertThat(response.userPointAfter()).isEqualTo(new BigDecimal("0"));
        verify(multiLock).unlock(); // 락이 해제되었는지 검증
        verify(redisPublisher).publish(any(String.class), any()); // 이벤트가 발행되었는지 검증
    }

    @Test
    @DisplayName("베팅 삭제에 성공 (분산 락)")
    void deleteBet_success() throws InterruptedException {
        //given
        Long userId = 1L;
        Long betId = 1L;
        Long matchId = 1L;

        User user = User.builder().username("test").build(); // 트랜잭션 서비스가 반환할 객체
        ReflectionTestUtils.setField(user, "id", userId);
        user.plusPoint(new BigDecimal("1000")); // 1000(기본)+1000(환불) = 2000

        Match match = Match.builder().build();
        ReflectionTestUtils.setField(match, "id", matchId);

        Bet bet = Bet.builder().user(user).match(match).betAmount(new BigDecimal("1000")).build();
        ReflectionTestUtils.setField(bet, "id", betId);

        // 트랜잭션 서비스가 반환할 데이터 Mocking
        BetTransactionService.DeleteBetData deleteBetData = new BetTransactionService.DeleteBetData(bet, user);

        // 락 획득 전 betId로 매치를 찾기 위한 Mocking (preFetch)
        given(betRepository.findById(betId)).willReturn(Optional.of(bet));

        // Redisson MultiLock Mocking
        given(redissonClient.getLock("LOCK:USER_POINT:" + userId)).willReturn(userLock);
        given(redissonClient.getLock("LOCK:MATCH:" + matchId)).willReturn(matchLock);
        given(redissonClient.getMultiLock(userLock, matchLock)).willReturn(multiLock);
        given(multiLock.tryLock(10, 5, TimeUnit.SECONDS)).willReturn(true); // 락 획득 성공

        // 트랜잭션 서비스 호출 Mocking
        given(betTransactionService.deleteBetInternal(userId, betId)).willReturn(deleteBetData);

        //when
        BetDeleteResponse response = betService.deleteBet(userId, betId);

        //then
        assertThat(response.refundAmount()).isEqualTo(new BigDecimal("1000"));
        assertThat(response.userPointAfter()).isEqualTo(new BigDecimal("2000")); // 기본 1000 + 환불 1000
        verify(multiLock).unlock(); // 락이 해제되었는지 검증
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