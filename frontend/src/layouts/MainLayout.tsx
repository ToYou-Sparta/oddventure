import React from 'react';
import type { ReactNode } from 'react';
import Header from './Header';
import Footer from './Footer';
import Chatbot from '../components/Chatbot';
import './MainLayout.css';

interface MainLayoutProps {
  children: ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="main-layout">
      <Header />
      <main className="main-content">{children}</main>
      <Footer />
      <Chatbot />
    </div>
  );
};

export default MainLayout;