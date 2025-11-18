import React, { useState, useEffect } from 'react';
import { matchService } from '../../services/matchService';
import type { Match } from '../../types';
import { MatchStatus, SelectedTeam } from '../../types';
import { betService } from '../../services/betService';
import { useAuth } from '../../contexts/AuthContext';
import { notificationService } from '../../services/notificationService';
import './MatchesPage.css';

const MatchesPage: React.FC = () => {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);
  const [selectedTeam, setSelectedTeam] = useState<SelectedTeam | null>(null);
  const [betAmount, setBetAmount] = useState('');
  const [betLoading, setBetLoading] = useState(false);

  // 검색 필터
  const [keyword, setKeyword] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const { user, refreshUser } = useAuth();

  useEffect(() => {
    fetchMatches();
  }, []);

  const fetchMatches = async () => {
    try {
      const response = await matchService.getMatches(0, 50);
      setMatches(response.content);
    } catch (error) {
      console.error('Failed to fetch matches:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    setLoading(true);
    try {
      const searchParams = {
        keyword: keyword || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        page: 0,
        size: 50,
      };

      const response = await matchService.searchMatchesES(searchParams);
      setMatches(response.content);
    } catch (error) {
      console.error('Failed to search matches:', error);
      alert('검색에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setKeyword('');
    setFromDate('');
    setToDate('');
    fetchMatches();
  };

  const getStatusBadge = (status: MatchStatus) => {
    const badges = {
      [MatchStatus.SCHEDULED]: { text: '예정', class: 'badge-scheduled' },
      [MatchStatus.ONGOING]: { text: '진행중', class: 'badge-ongoing' },
      [MatchStatus.FINISHED]: { text: '종료', class: 'badge-finished' },
    };
    const badge = badges[status];
    return <span className={`badge ${badge.class}`}>{badge.text}</span>;
  };

  const calculateOdds = (match: Match, team: SelectedTeam) => {
    const totalA = match.totalAmountA || 1;
    const totalB = match.totalAmountB || 1;
    const odds = team === SelectedTeam.Team_A ? totalB / totalA : totalA / totalB;
    return odds.toFixed(2);
  };

  const handleBetClick = (match: Match) => {
    if (match.status !== MatchStatus.SCHEDULED) {
      alert('배팅은 예정된 경기에만 가능합니다.');
      return;
    }
    setSelectedMatch(match);
    setSelectedTeam(null);
    setBetAmount('');
  };

  const handleBetSubmit = async () => {
    if (!selectedMatch || !selectedTeam || !betAmount) {
      alert('모든 정보를 입력해주세요.');
      return;
    }

    const amount = Number(betAmount);
    if (amount <= 0) {
      alert('배팅 금액은 0보다 커야 합니다.');
      return;
    }

    if (user && amount > user.point) {
      alert('포인트가 부족합니다.');
      return;
    }

    setBetLoading(true);

    try {
      await betService.createBet({
        matchId: selectedMatch.matchId,
        selectedTeam,
        betAmount: amount,
      });

      // 배팅 성공 시 해당 경기의 배당률 알림 구독
      notificationService.subscribeToMatchOdds(selectedMatch.matchId);

      alert('배팅이 완료되었습니다!');
      setSelectedMatch(null);
      await refreshUser();
      await fetchMatches();
    } catch (error: any) {
      alert(error.response?.data?.message || '배팅에 실패했습니다.');
    } finally {
      setBetLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">경기 목록을 불러오는 중...</div>;
  }

  return (
    <div className="container">
      <div className="matches-header">
        <h1>경기 목록</h1>
        <p className="text-muted">원하는 경기에 배팅해보세요</p>
      </div>

      {/* 검색 필터 */}
      <div className="card mb-4">
        <div className="search-filters">
          <div className="form-group">
            <label className="label">키워드</label>
            <input
              type="text"
              className="input"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="경기명 또는 팀 이름을 입력하세요"
            />
          </div>

          <div className="form-group">
            <label className="label">시작일</label>
            <input
              type="date"
              className="input"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="label">종료일</label>
            <input
              type="date"
              className="input"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>

          <div className="search-actions">
            <button onClick={handleSearch} className="btn btn-primary">
              검색
            </button>
            <button onClick={handleReset} className="btn btn-outline">
              초기화
            </button>
          </div>
        </div>
      </div>

      <div className="matches-grid">
        {matches.map((match) => (
          <div key={match.matchId} className="match-card card">
            <div className="match-header">
              <h3 className="match-name">{match.matchName}</h3>
              {getStatusBadge(match.status)}
            </div>

            <div className="match-teams">
              <div className="team">
                <div className="team-name">{match.teamA}</div>
                <div className="team-odds">
                  배당 {calculateOdds(match, SelectedTeam.Team_A)}
                </div>
                <div className="team-total text-muted">
                  {match.totalAmountA.toLocaleString()}P
                </div>
              </div>

              <div className="vs">VS</div>

              <div className="team">
                <div className="team-name">{match.teamB}</div>
                <div className="team-odds">
                  배당 {calculateOdds(match, SelectedTeam.Team_B)}
                </div>
                <div className="team-total text-muted">
                  {match.totalAmountB.toLocaleString()}P
                </div>
              </div>
            </div>

            <div className="match-info">
              <div className="info-item">
                <span className="info-label">시작 시간</span>
                <span>{new Date(match.startTime).toLocaleString('ko-KR')}</span>
              </div>
              <div className="info-item">
                <span className="info-label">조회수</span>
                <span>{match.viewCount.toLocaleString()}</span>
              </div>
            </div>

            {match.status === MatchStatus.SCHEDULED && (
              <button
                onClick={() => handleBetClick(match)}
                className="btn btn-primary btn-full mt-2"
              >
                배팅하기
              </button>
            )}

            {match.status === MatchStatus.FINISHED && match.winner && (
              <div className="match-result">
                <strong>승리:</strong> {match.winner}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 배팅 모달 */}
      {selectedMatch && (
        <div className="modal-overlay" onClick={() => setSelectedMatch(null)}>
          <div className="modal-content card" onClick={(e) => e.stopPropagation()}>
            <h2 className="mb-3">{selectedMatch.matchName}</h2>

            <div className="form-group">
              <label className="label">팀 선택</label>
              <div className="team-select-group">
                <button
                  className={`team-select-btn ${
                    selectedTeam === SelectedTeam.Team_A ? 'active' : ''
                  }`}
                  onClick={() => setSelectedTeam(SelectedTeam.Team_A)}
                >
                  {selectedMatch.teamA}
                  <br />
                  <small>배당 {calculateOdds(selectedMatch, SelectedTeam.Team_A)}</small>
                </button>
                <button
                  className={`team-select-btn ${
                    selectedTeam === SelectedTeam.Team_B ? 'active' : ''
                  }`}
                  onClick={() => setSelectedTeam(SelectedTeam.Team_B)}
                >
                  {selectedMatch.teamB}
                  <br />
                  <small>배당 {calculateOdds(selectedMatch, SelectedTeam.Team_B)}</small>
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="label">배팅 금액</label>
              <input
                type="number"
                className="input"
                value={betAmount}
                onChange={(e) => setBetAmount(e.target.value)}
                placeholder="배팅할 포인트를 입력하세요"
              />
              <small className="text-muted">보유 포인트: {user?.point.toLocaleString()}P</small>
            </div>

            {selectedTeam && betAmount && (
              <div className="bet-preview">
                <p>
                  <strong>예상 당첨금:</strong>{' '}
                  {(
                    Number(betAmount) *
                    Number(calculateOdds(selectedMatch, selectedTeam))
                  ).toLocaleString()}
                  P
                </p>
              </div>
            )}

            <div className="modal-actions">
              <button
                onClick={() => setSelectedMatch(null)}
                className="btn btn-outline"
                disabled={betLoading}
              >
                취소
              </button>
              <button
                onClick={handleBetSubmit}
                className="btn btn-primary"
                disabled={betLoading}
              >
                {betLoading ? '배팅 중...' : '배팅 확인'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MatchesPage;