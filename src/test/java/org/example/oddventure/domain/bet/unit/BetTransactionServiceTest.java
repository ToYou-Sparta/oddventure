package org.example.oddventure.domain.bet.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.entity.Bet;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.exception.BetErrorCode;
import org.example.oddventure.domain.bet.exception.BetException;
import org.example.oddventure.domain.bet.repository.BetRepository;
import org.example.oddventure.domain.bet.service.BetTransactionService;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class BetTransactionServiceTest {

    @InjectMocks
    private BetTransactionService betTransactionService;

    @Mock
    private BetRepository betRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Match match;

    @BeforeEach
    void setUp() {
        // 공통 유저 Mock 생성 (기본 1000 포인트)
        user = User.builder()
                .username("test")
                .email("test1234@test.com")
                .password("test1234!")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        // 공통 매치 Mock 생성 (SCHEDULED 상태)
        match = Match.builder()
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(match, "id", 1L);

        // calculateOdds()가 0으로 나누기 오류를 방지하도록 초기값 설정
        match.plusTeamA(new BigDecimal("100"));
        match.plusTeamB(new BigDecimal("100"));
    }

    @Nested
    @DisplayName("베팅 생성 트랜잭션")
    class CreateBetInternal {

        @Test
        @DisplayName("성공 - 유저 포인트가 차감되고 베팅 금액이 저장된다")
        void createBetInternal_success() {
            //given
            BetCreateRequest request = new BetCreateRequest(1L, SelectedTeam.Team_A, 100L);
            BigDecimal initialPoint = user.getPoint(); // 1000

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(matchRepository.findById(1L)).willReturn(Optional.of(match));
            given(betRepository.save(any(Bet.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            BetTransactionService.CreateBetData data = betTransactionService.createBetInternal(1L, request);

            //then
            assertThat(data.userPointAfter()).isEqualTo(initialPoint.subtract(new BigDecimal(100))); // 900
            assertThat(user.getPoint()).isEqualTo(new BigDecimal("900")); // User 객체 상태 변경 확인
            assertThat(match.getTotalAmountA()).isEqualTo(new BigDecimal("1200")); // 1100(초기값) + 100(베팅)
        }

        @Test
        @DisplayName("실패 - 보유 포인트 부족")
        void createBetInternal_fail_notEnoughPoints() {
            //given
            // 2000 포인트 베팅 요청 (보유: 1000)
            BetCreateRequest request = new BetCreateRequest(1L, SelectedTeam.Team_A, 2000L);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            //when & then
            BetException exception = assertThrows(BetException.class, () -> {
                betTransactionService.createBetInternal(1L, request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(BetErrorCode.NOT_ENOUGH_POINTS);
        }

        @Test
        @DisplayName("실패 - 베팅 불가능한 매치 (ONGOING)")
        void createBetInternal_fail_matchNotBettable() {
            //given
            match.setStatus(MatchStatus.ONGOING); // 경기 상태 변경
            BetCreateRequest request = new BetCreateRequest(1L, SelectedTeam.Team_A, 100L);

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(matchRepository.findById(1L)).willReturn(Optional.of(match));

            //when & then
            BetException exception = assertThrows(BetException.class, () -> {
                betTransactionService.createBetInternal(1L, request);
            });
            assertThat(exception.getErrorCode()).isEqualTo(BetErrorCode.MATCH_NOT_BETTABLE);
        }
    }

    @Nested
    @DisplayName("베팅 취소 트랜잭션")
    class DeleteBetInternal {

        @Test
        @DisplayName("성공 - 포인트가 환불되고 베팅 금액이 복구된다")
        void deleteBetInternal_success() {
            //given
            Long userId = 1L;
            Long betId = 1L;
            BigDecimal betAmount = new BigDecimal("100");

            Bet bet = Bet.builder()
                    .user(user)
                    .match(match)
                    .selectedTeam(SelectedTeam.Team_A)
                    .betAmount(betAmount)
                    .oddsAtBetting(new BigDecimal("1.5"))
                    .build();

            BigDecimal initialPoint = user.getPoint(); // 1000
            BigDecimal initialMatchAmount = match.getTotalAmountA(); // 100

            given(betRepository.findByIdForDelete(betId)).willReturn(Optional.of(bet));
            given(matchRepository.findById(match.getId())).willReturn(Optional.of(match));

            //when
            BetTransactionService.DeleteBetData data = betTransactionService.deleteBetInternal(userId, betId);

            //then
            assertThat(bet.isDeleted()).isTrue(); // 논리적 삭제 확인
            assertThat(user.getPoint()).isEqualTo(initialPoint.add(betAmount)); // 1100
            assertThat(match.getTotalAmountA()).isEqualTo(initialMatchAmount.subtract(betAmount)); // 0
            assertThat(data.user().getPoint()).isEqualTo(new BigDecimal("1100"));
        }

        @Test
        @DisplayName("실패 - 베팅 소유자가 아님")
        void deleteBetInternal_fail_permissionDenied() {
            //given
            Long otherUserId = 2L; // 다른 유저 ID
            Long betId = 1L;

            Bet bet = Bet.builder().user(user).match(match).build(); // bet은 1L 유저 소유

            given(betRepository.findByIdForDelete(betId)).willReturn(Optional.of(bet));

            //when & then
            // 2L 유저가 1L 유저의 베팅 취소 시도
            BetException exception = assertThrows(BetException.class, () -> {
                betTransactionService.deleteBetInternal(otherUserId, betId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(BetErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 취소 불가능한 매치 (FINISHED)")
        void deleteBetInternal_fail_matchNotCancelable() {
            //given
            Long userId = 1L;
            Long betId = 1L;
            match.finishMatch("T1", "GEN.G"); // 경기 상태 FINISHED로 변경

            Bet bet = Bet.builder().user(user).match(match).build();

            given(betRepository.findByIdForDelete(betId)).willReturn(Optional.of(bet));
            given(matchRepository.findById(match.getId())).willReturn(Optional.of(match));

            //when & then
            BetException exception = assertThrows(BetException.class, () -> {
                betTransactionService.deleteBetInternal(userId, betId);
            });
            assertThat(exception.getErrorCode()).isEqualTo(BetErrorCode.MATCH_NOT_CANCELABLE);
        }
    }
}