import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { chatService, type ChatMessage } from '../services/chatService';
import './Chatbot.css';

const Chatbot: React.FC = () => {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (user && isOpen && !isConnected) {
      chatService.connect(
        user.id,
        (message: string) => {
          // AI 응답 수신
          setMessages((prev) => [
            ...prev,
            {
              role: 'assistant',
              content: message,
              timestamp: new Date(),
            },
          ]);
          setIsSending(false);
        },
        (connected: boolean) => {
          setIsConnected(connected);
        }
      );
    }

    return () => {
      if (!isOpen) {
        chatService.disconnect();
        setIsConnected(false);
      }
    };
  }, [user, isOpen, isConnected]);

  useEffect(() => {
    // 메시지가 추가될 때마다 스크롤을 아래로
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = async () => {
    if (!inputMessage.trim() || isSending) return;

    const userMessage: ChatMessage = {
      role: 'user',
      content: inputMessage,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage('');
    setIsSending(true);

    try {
      await chatService.sendMessage(inputMessage);
    } catch (error) {
      console.error('Failed to send message:', error);
      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          content: '죄송합니다. 메시지 전송에 실패했습니다.',
          timestamp: new Date(),
        },
      ]);
      setIsSending(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const toggleChat = () => {
    setIsOpen(!isOpen);
  };

  if (!user) return null;

  return (
    <>
      {/* 플로팅 버튼 */}
      <button className="chatbot-toggle-button" onClick={toggleChat} aria-label="챗봇 열기">
        {isOpen ? (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        ) : (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
        )}
      </button>

      {/* 챗봇 창 */}
      {isOpen && (
        <div className="chatbot-container">
          <div className="chatbot-header">
            <div className="chatbot-header-content">
              <div className="chatbot-icon">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <div>
                <h3>CS2 AI 어시스턴트</h3>
                <span className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
                  {isConnected ? '연결됨' : '연결 중...'}
                </span>
              </div>
            </div>
            <button className="close-button" onClick={toggleChat} aria-label="닫기">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <div className="chatbot-messages">
            {messages.length === 0 && (
              <div className="welcome-message">
                <p>안녕하세요! CS2 경기에 대해 무엇이든 물어보세요.</p>
                <div className="suggestions">
                  <button onClick={() => setInputMessage('오늘 경기 일정 알려줘')}>
                    오늘 경기 일정
                  </button>
                  <button onClick={() => setInputMessage('인기 검색어 알려줘')}>
                    인기 검색어
                  </button>
                  <button onClick={() => setInputMessage('CS2 뉴스 알려줘')}>
                    CS2 뉴스
                  </button>
                </div>
              </div>
            )}
            {messages.map((msg, index) => (
              <div key={index} className={`message ${msg.role}`}>
                <div className="message-content">
                  <p>{msg.content}</p>
                  {msg.timestamp && (
                    <span className="timestamp">
                      {new Date(msg.timestamp).toLocaleTimeString('ko-KR', {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  )}
                </div>
              </div>
            ))}
            {isSending && (
              <div className="message assistant">
                <div className="message-content">
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-input">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="메시지를 입력하세요..."
              disabled={!isConnected || isSending}
            />
            <button
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() || !isConnected || isSending}
              aria-label="전송"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <line x1="22" y1="2" x2="11" y2="13"></line>
                <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
              </svg>
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default Chatbot;