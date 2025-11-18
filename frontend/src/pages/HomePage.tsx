import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './HomePage.css';

const HomePage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div className="home-page">
      <div className="hero-section">
        <div className="container">
          <h1 className="hero-title">ODDVENTURE</h1>
          <p className="hero-subtitle">CS2 E-Sports Betting Platform</p>
          <p className="hero-description">
            최고의 CS2 경기에 배팅하고 승리의 기쁨을 누리세요
          </p>

          <div className="hero-actions">
            {user ? (
              <>
                <Link to="/matches" className="btn btn-primary btn-lg">
                  경기 보러가기
                </Link>
                <Link to="/my-bets" className="btn btn-secondary btn-lg">
                  내 배팅 확인
                </Link>
              </>
            ) : (
              <>
                <Link to="/signup" className="btn btn-primary btn-lg">
                  시작하기
                </Link>
                <Link to="/login" className="btn btn-outline btn-lg">
                  로그인
                </Link>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="features-section">
        <div className="container">
          <div className="features-grid">
            <div className="feature-card card">
              <div className="feature-icon">🎮</div>
              <h3>실시간 경기</h3>
              <p className="text-muted">
                GRID API를 통해 실시간으로 업데이트되는 CS2 경기 정보
              </p>
            </div>

            <div className="feature-card card">
              <div className="feature-icon">💰</div>
              <h3>동적 배당률</h3>
              <p className="text-muted">
                배팅 현황에 따라 실시간으로 변화하는 공정한 배당률 시스템
              </p>
            </div>

            <div className="feature-card card">
              <div className="feature-icon">🔒</div>
              <h3>안전한 시스템</h3>
              <p className="text-muted">
                JWT 인증과 분산 락을 통한 안전하고 신뢰할 수 있는 플랫폼
              </p>
            </div>

            <div className="feature-card card">
              <div className="feature-icon">🚀</div>
              <h3>빠른 검색</h3>
              <p className="text-muted">
                Elasticsearch 기반 전문 검색으로 원하는 경기를 빠르게 찾기
              </p>
            </div>
          </div>
        </div>
      </div>

      {user && (
        <div className="stats-section">
          <div className="container">
            <div className="stats-card card">
              <h2>내 현황</h2>
              <div className="stats-grid">
                <div className="stat-item">
                  <div className="stat-label">보유 포인트</div>
                  <div className="stat-value">{user.point.toLocaleString()}P</div>
                </div>
                <div className="stat-item">
                  <div className="stat-label">계정 등급</div>
                  <div className="stat-value">
                    {user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default HomePage;