import React from 'react';
import './Footer.css';

const Footer: React.FC = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <p className="copyright">
            &copy; 2025 ODDVENTURE. All rights reserved.
          </p>
          <p className="description">
            CS2 E-Sports Betting Platform
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;