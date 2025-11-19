import React, { useState, useEffect } from 'react';
import { betService } from '../../services/betService';
import type { Bet } from '../../types';
import { SelectedTeam } from '../../types';
import { useAuth } from '../../contexts/AuthContext';
import './MyBetsPage.css';

const MyBetsPage: React.FC = () => {
  const [bets, setBets] = useState<Bet[]>([]);
  const [loading, setLoading] = useState(true);
  const { refreshUser } = useAuth();

  useEffect(() => {
    fetchMyBets();
  }, []);

  const fetchMyBets = async () => {
    try {
      const response = await betService.getMyBets(0, 50);
      setBets(response.content);
    } catch (error) {
      console.error('Failed to fetch bets:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelBet = async (betId: number) => {
    if (!confirm('배팅을 취소하시겠습니까?')) {
      return;
    }

    try {
      await betService.cancelBet(betId);
      alert('배팅이 취소되었습니다.');
      await fetchMyBets();
      await refreshUser();
    } catch (error: any) {
      alert(error.response?.data?.message || '배팅 취소에 실패했습니다.');
    }
  };

  const getSelectedTeamName = (bet: Bet) => {
    return bet.selectedTeam === SelectedTeam.Team_A ? bet.matchBetResponse.teamA : bet.matchBetResponse.teamB;
  };

  const getExpectedReward = (bet: Bet) => {
    return (bet.betAmount * bet.oddsAtBetting).toFixed(0);
  };

  const getBetStatus = (bet: Bet) => {
    if (bet.matchBetResponse.status === 'SCHEDULED') {
      return { text: '대기중', class: 'status-waiting' };
    }
    if (bet.matchBetResponse.status === 'ONGOING') {
      return { text: '진행중', class: 'status-ongoing' };
    }
    if (bet.matchBetResponse.status === 'FINISHED') {
      if (bet.isWin) {
        return { text: '당첨', class: 'status-win' };
      } else {
        return { text: '낙첨', class: 'status-lose' };
      }
    }
    return { text: '알수없음', class: 'status-unknown' };
  };

  if (loading) {
    return <div className="loading">배팅 내역을 불러오는 중...</div>;
  }

  return (
    <div className="container">
      <div className="my-bets-header">
        <h1>내 배팅 내역</h1>
        <p className="text-muted">나의 배팅 현황을 확인하세요</p>
      </div>

      {bets.length === 0 ? (
        <div className="empty-state card">
          <p>아직 배팅 내역이 없습니다.</p>
          <a href="/matches" className="btn btn-primary mt-2">
            경기 보러가기
          </a>
        </div>
      ) : (
        <div className="bets-list">
          {bets.map((bet) => {
            const status = getBetStatus(bet);
            return (
              <div key={bet.betId} className="bet-card card">
                <div className="bet-header">
                  <div>
                    <h3 className="bet-match-name">{bet.matchBetResponse.matchName}</h3>
                    <p className="bet-date text-muted">
                      {new Date(bet.createdAt).toLocaleString('ko-KR')}
                    </p>
                  </div>
                  <span className={`bet-status ${status.class}`}>{status.text}</span>
                </div>

                <div className="bet-details">
                  <div className="detail-row">
                    <span className="detail-label">선택한 팀</span>
                    <span className="detail-value">{getSelectedTeamName(bet)}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">배팅 금액</span>
                    <span className="detail-value">{bet.betAmount.toLocaleString()}P</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">배당률</span>
                    <span className="detail-value">{bet.oddsAtBetting.toFixed(2)}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">예상 당첨금</span>
                    <span className="detail-value highlight">
                      {getExpectedReward(bet).toLocaleString()}P
                    </span>
                  </div>
                </div>

                {bet.matchBetResponse.status === 'SCHEDULED' && (
                  <button
                    onClick={() => handleCancelBet(bet.betId)}
                    className="btn btn-outline btn-full mt-2"
                  >
                    배팅 취소
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default MyBetsPage;