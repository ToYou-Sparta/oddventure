import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './Header.css';

const Header: React.FC = () => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <Link to="/" className="logo">
            <h1>ODDVENTURE</h1>
          </Link>

          <nav className="nav">
            {user ? (
              <>
                <Link to="/matches" className="nav-link">
                  경기
                </Link>
                <Link to="/my-bets" className="nav-link">
                  내 배팅
                </Link>
                {isAdmin() && (
                  <Link to="/admin" className="nav-link">
                    관리자
                  </Link>
                )}
                <div className="user-info">
                  <span className="username">{user.username}</span>
                  <span className="points">{user.point.toLocaleString()}P</span>
                </div>
                <button onClick={handleLogout} className="btn btn-outline">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-primary">
                  로그인
                </Link>
                <Link to="/signup" className="btn btn-secondary">
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Header;