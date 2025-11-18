import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080';

export interface MatchStatusNotification {
  matchId: number;
  matchName: string;
  teamA: string;
  teamB: string;
  startTime: string;
  status: 'SCHEDULED' | 'ONGOING' | 'FINISHED';
}

export interface OddsChangeNotification {
  matchId: number;
  selectedTeam: string;
  odds: number;
}

export type NotificationCallback = (notification: MatchStatusNotification | OddsChangeNotification, type: 'status' | 'odds') => void;

class NotificationService {
  private client: Client | null = null;
  private userId: number | null = null;
  private subscribedMatches: Set<number> = new Set();
  private connectionCallback: ((connected: boolean) => void) | null = null;
  private notificationCallback: NotificationCallback | null = null;

  connect(userId: number, onNotification: NotificationCallback, onConnection?: (connected: boolean) => void) {
    console.log('[NotificationService] 🔌 Connect called with userId:', userId);

    if (this.client?.connected) {
      console.log('[NotificationService] ⚠️ Already connected, updating callbacks');
      this.notificationCallback = onNotification;
      this.connectionCallback = onConnection || null;
      return;
    }

    this.userId = userId;
    this.notificationCallback = onNotification;
    this.connectionCallback = onConnection || null;

    console.log('[NotificationService] ✅ Callbacks stored:', {
      hasNotificationCallback: !!this.notificationCallback,
      hasConnectionCallback: !!this.connectionCallback
    });

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      debug: (str) => {
        console.log('STOMP Notification Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        console.log('[NotificationService] WebSocket Connected', frame);
        console.log('[NotificationService] Session info:', {
          userId: this.userId,
          sessionId: (frame as any)?.headers?.['user-name']
        });
        this.connectionCallback?.(true);
        this.subscribeToUserQueue();
      },
      onStompError: (frame) => {
        console.error('STOMP Notification Error:', frame);
        this.connectionCallback?.(false);
      },
      onWebSocketClose: () => {
        console.log('Notification WebSocket Closed');
        this.connectionCallback?.(false);
        this.subscribedMatches.clear();
      },
    });

    this.client.activate();
  }

  // 사용자별 배당률 알림 구독 (초기화)
  private subscribeToUserQueue() {
    console.log('[NotificationService] Initial queue subscription - will subscribe to specific matches via subscribeToMatchOdds()');
  }

  // 특정 경기의 배당률 변경 구독
  subscribeToMatchOdds(matchId: number) {
    if (!this.client?.connected || !this.userId) {
      console.warn('[NotificationService] Cannot subscribe - client not connected or no userId');
      return;
    }

    if (this.subscribedMatches.has(matchId)) {
      console.log(`[NotificationService] Already subscribed to odds for match ${matchId}`);
      return;
    }

    const destination = `/user/${this.userId}/queue/matches/${matchId}/odds`;

    console.log(`[NotificationService] Subscribing to ${destination}`);

    this.client.subscribe(destination, (message) => {
      console.log('[NotificationService] 📨 Received odds notification:', {
        matchId,
        destination,
        body: message.body,
        headers: message.headers
      });

      console.log('[NotificationService] 🔍 Callback status:', {
        hasCallback: !!this.notificationCallback,
        callbackType: typeof this.notificationCallback
      });

      try {
        const data: OddsChangeNotification = JSON.parse(message.body);
        console.log('[NotificationService] ✅ Parsed odds data:', data);

        if (this.notificationCallback) {
          console.log('[NotificationService] 🚀 Calling notification callback with:', { data, type: 'odds' });
          this.notificationCallback(data, 'odds');
          console.log('[NotificationService] ✅ Callback executed');
        } else {
          console.error('[NotificationService] ❌ No notification callback registered!');
        }
      } catch (error) {
        console.error('[NotificationService] ❌ Failed to parse odds notification:', error);
      }
    });

    this.subscribedMatches.add(matchId);
    console.log('[NotificationService] Successfully subscribed to match odds:', matchId);
  }

  // 특정 경기 상태 변경 구독
  subscribeToMatch(matchId: number) {
    if (!this.client?.connected || this.subscribedMatches.has(matchId)) return;

    const destination = `/topic/matches/${matchId}/status`;
    this.client.subscribe(destination, (message) => {
      const data: MatchStatusNotification = JSON.parse(message.body);
      console.log('Match status notification:', data);
      this.notificationCallback?.(data, 'status');
    });

    this.subscribedMatches.add(matchId);
  }

  // 경기 구독 해제
  unsubscribeFromMatch(matchId: number) {
    this.subscribedMatches.delete(matchId);
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.userId = null;
    this.subscribedMatches.clear();
    this.notificationCallback = null;
    this.connectionCallback = null;
  }

  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

export const notificationService = new NotificationService();