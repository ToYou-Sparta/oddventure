import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { matchService } from '../../services/matchService';
import type { Match, User } from '../../types';
import './AdminPage.css';

const AdminPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'matches' | 'users'>('matches');
  const [matches, setMatches] = useState<Match[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (activeTab === 'matches') {
      fetchMatches();
    } else {
      fetchUsers();
    }
  }, [activeTab]);

  const fetchMatches = async () => {
    setLoading(true);
    try {
      const response = await matchService.getMatches(0, 50);
      setMatches(response.content);
    } catch (error) {
      console.error('Failed to fetch matches:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await adminService.getUsers(0, 50);
      setUsers(response.content);
    } catch (error) {
      console.error('Failed to fetch users:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFetchMatches = async () => {
    if (!confirm('GRID API에서 경기를 가져오시겠습니까?')) {
      return;
    }

    setLoading(true);
    try {
      await adminService.fetchMatches();
      alert('경기를 성공적으로 가져왔습니다.');
      await fetchMatches();
    } catch (error: any) {
      alert(error.response?.data?.message || '경기 가져오기에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleFetchResult = async (fetchId: string) => {
    if (!confirm('GRID API에서 경기 결과를 가져오시겠습니까?')) {
      return;
    }

    setLoading(true);
    try {
      await adminService.fetchMatchResult(fetchId);
      alert('경기 결과를 성공적으로 가져왔습니다.');
      await fetchMatches();
    } catch (error: any) {
      alert(error.response?.data?.message || '경기 결과 가져오기에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSyncES = async () => {
    if (!confirm('Elasticsearch와 동기화하시겠습니까?')) {
      return;
    }

    setLoading(true);
    try {
      await adminService.syncElasticsearch();
      alert('Elasticsearch 동기화가 완료되었습니다.');
    } catch (error: any) {
      alert(error.response?.data?.message || 'Elasticsearch 동기화에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleAdjustPoints = async (userId: number) => {
    const pointsInput = prompt('조정할 포인트를 입력하세요 (음수는 차감):');
    if (!pointsInput) return;

    const points = Number(pointsInput);
    if (isNaN(points)) {
      alert('올바른 숫자를 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      await adminService.adjustUserPoints(userId, points);
      alert('포인트가 조정되었습니다.');
      await fetchUsers();
    } catch (error: any) {
      alert(error.response?.data?.message || '포인트 조정에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="admin-header">
        <h1>관리자 페이지</h1>
        <p className="text-muted">경기와 사용자를 관리하세요</p>
      </div>

      <div className="admin-tabs">
        <button
          className={`tab-btn ${activeTab === 'matches' ? 'active' : ''}`}
          onClick={() => setActiveTab('matches')}
        >
          경기 관리
        </button>
        <button
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          사용자 관리
        </button>
      </div>

      {activeTab === 'matches' && (
        <div className="admin-section">
          <div className="admin-actions">
            <button onClick={handleFetchMatches} className="btn btn-primary" disabled={loading}>
              GRID에서 경기 가져오기
            </button>
            <button onClick={handleSyncES} className="btn btn-secondary" disabled={loading}>
              Elasticsearch 동기화
            </button>
          </div>

          {loading ? (
            <div className="loading">로딩 중...</div>
          ) : (
            <div className="admin-table-container">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>경기명</th>
                    <th>팀 A</th>
                    <th>팀 B</th>
                    <th>상태</th>
                    <th>시작 시간</th>
                    <th>승자</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {matches.map((match) => (
                    <tr key={match.matchId}>
                      <td>{match.matchId}</td>
                      <td>{match.matchName}</td>
                      <td>
                        {match.teamA}
                        <br />
                        <small className="text-muted">
                          {match.totalAmountA.toLocaleString()}P
                        </small>
                      </td>
                      <td>
                        {match.teamB}
                        <br />
                        <small className="text-muted">
                          {match.totalAmountB.toLocaleString()}P
                        </small>
                      </td>
                      <td>
                        <span className={`status-badge status-${match.status.toLowerCase()}`}>
                          {match.status}
                        </span>
                      </td>
                      <td>{new Date(match.startTime).toLocaleString('ko-KR')}</td>
                      <td>{match.winner || '-'}</td>
                      <td>
                        {match.status === 'ONGOING' && (
                          <button
                            onClick={() => handleFetchResult(match.fetchId)}
                            className="btn btn-sm btn-secondary"
                            disabled={loading}
                          >
                            결과 가져오기
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === 'users' && (
        <div className="admin-section">
          {loading ? (
            <div className="loading">로딩 중...</div>
          ) : (
            <div className="admin-table-container">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>사용자명</th>
                    <th>이메일</th>
                    <th>권한</th>
                    <th>포인트</th>
                    <th>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((user) => (
                    <tr key={user.userId}>
                      <td>{user.userId}</td>
                      <td>{user.username}</td>
                      <td>{user.email}</td>
                      <td>
                        <span
                          className={`role-badge ${
                            user.role === 'ROLE_ADMIN' ? 'role-admin' : 'role-user'
                          }`}
                        >
                          {user.role.replace('ROLE_', '')}
                        </span>
                      </td>
                      <td>{user.point.toLocaleString()}P</td>
                      <td>
                        <button
                          onClick={() => handleAdjustPoints(user.userId)}
                          className="btn btn-sm btn-secondary"
                          disabled={loading}
                        >
                          포인트 조정
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminPage;