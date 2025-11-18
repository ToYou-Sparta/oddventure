import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { notificationService } from '../services/notificationService';
import { betService } from '../services/betService';
import type { MatchStatusNotification, OddsChangeNotification } from '../services/notificationService';
import './NotificationToast.css';

interface Toast {
  id: number;
  type: 'status' | 'odds';
  message: string;
  data: MatchStatusNotification | OddsChangeNotification;
}

const NotificationToast: React.FC = () => {
  const { user } = useAuth();
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (!user) return;

    // 알림 서비스 연결
    notificationService.connect(
        user.userId,
        (notification, type) => {
          console.log('[NotificationToast] 🔔 Notification received!', { notification, type });

          const toast: Toast = {
            id: Date.now(),
            type,
            message: formatNotificationMessage(notification, type),
            data: notification,
          };

          console.log('[NotificationToast] 📝 Toast created:', toast);

          setToasts((prev) => {
            const newToasts = [...prev, toast];
            console.log('[NotificationToast] 📋 Updated toasts array:', newToasts);
            return newToasts;
          });

          // 5초 후 자동 제거
          setTimeout(() => {
            console.log('[NotificationToast] ⏱️ Removing toast:', toast.id);
            setToasts((prev) => prev.filter((t) => t.id !== toast.id));
          }, 5000);
        },
        (connected) => {
          console.log('[NotificationToast] 🔌 Connection status changed:', connected);
          setIsConnected(connected);

          // WebSocket 연결 성공 시 배팅한 경기 구독
          if (connected) {
            subscribeToMyBets();
          }
        }
    );

    return () => {
      notificationService.disconnect();
    };
  }, [user]);

  // 내가 배팅한 경기들에 대해 배당률 알림 구독
  const subscribeToMyBets = async () => {
    try {
      console.log('[NotificationToast] 📡 Fetching my bets...');
      const response = await betService.getMyBets(0, 100); // 최근 100개 배팅 조회

      console.log('[NotificationToast] 📊 My bets response:', response);

      if (response.content && response.content.length > 0) {
        // 중복 제거 (같은 경기에 여러 번 배팅했을 수 있음)
        const uniqueMatchIds = [...new Set(response.content.map(bet => bet.matchBetResponse.matchId))];

        console.log('[NotificationToast] 🎯 Subscribing to match IDs:', uniqueMatchIds);

        uniqueMatchIds.forEach(matchId => {
          notificationService.subscribeToMatchOdds(matchId);
        });
      } else {
        console.log('[NotificationToast] ℹ️ No bets found');
      }
    } catch (error) {
      console.error('[NotificationToast] ❌ Failed to fetch my bets:', error);
    }
  };

  const formatNotificationMessage = (
      notification: MatchStatusNotification | OddsChangeNotification,
      type: 'status' | 'odds'
  ): string => {
    if (type === 'status') {
      const data = notification as MatchStatusNotification;
      const statusText = {
        SCHEDULED: '예정됨',
        ONGOING: '진행 중',
        FINISHED: '종료됨',
      }[data.status];
      return `🏆 ${data.matchName} 경기가 ${statusText} 상태로 변경되었습니다.`;
    } else {
      const data = notification as OddsChangeNotification;
      return `📊 배팅한 경기의 ${data.selectedTeam} 팀 배당률이 ${data.odds.toFixed(2)}배로 변경되었습니다.`;
    }
  };

  const removeToast = (id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  if (!user) {
    console.log('[NotificationToast] ❌ No user, not rendering');
    return null;
  }

  console.log('[NotificationToast] 🎨 Rendering with toasts:', toasts);
  console.log('[NotificationToast] 🔢 Toasts count:', toasts.length);

  return (
      <>
        {/* 연결 상태 표시 (디버깅용, 필요시 제거 가능) */}
        <div className={`notification-status ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? '🟢 알림 연결됨' : '🔴 알림 연결 안됨'}
        </div>

        {/* 토스트 알림 */}
        <div className="notification-toast-container">
          {toasts.map((toast) => {
            console.log('[NotificationToast] 🎯 Rendering toast:', toast);
            return (
                <div
                    key={toast.id}
                    className={`notification-toast ${toast.type}`}
                    onClick={() => removeToast(toast.id)}
                >
                  <div className="toast-content">
                    <p>{toast.message}</p>
                  </div>
                  <button className="toast-close" onClick={() => removeToast(toast.id)}>
                    ×
                  </button>
                </div>
            );
          })}
        </div>
      </>
  );
};

export default NotificationToast;