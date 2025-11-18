import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp?: Date;
}

class ChatService {
  private client: Client | null = null;
  private userId: number | null = null;
  private messageCallback: ((message: string) => void) | null = null;
  private connectionCallback: ((connected: boolean) => void) | null = null;

  connect(userId: number, onMessage: (message: string) => void, onConnection?: (connected: boolean) => void) {
    this.userId = userId;
    this.messageCallback = onMessage;
    this.connectionCallback = onConnection || null;

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket Connected');
        this.connectionCallback?.(true);
        this.subscribe();
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame);
        this.connectionCallback?.(false);
      },
      onWebSocketClose: () => {
        console.log('WebSocket Closed');
        this.connectionCallback?.(false);
      },
    });

    this.client.activate();
  }

  private subscribe() {
    if (!this.client || !this.userId) return;

    const destination = `/topic/chat/${this.userId}`;
    this.client.subscribe(destination, (message) => {
      console.log('Received message:', message.body);
      this.messageCallback?.(message.body);
    });
  }

  sendMessage(message: string) {
    // Redis Pub/Sub을 통해 메시지 전송
    // 백엔드에서는 chat:{userId}:input 채널을 구독하고 있음
    // HTTP API를 통해 전송
    return this.sendViaHttp(message);
  }

  private async sendViaHttp(message: string) {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`${WS_BASE_URL}/api/v1/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      credentials: 'include',
      body: JSON.stringify({ message }),
    });

    if (!response.ok) {
      throw new Error('Failed to send message');
    }

    return response.json();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.userId = null;
    this.messageCallback = null;
    this.connectionCallback = null;
  }

  isConnected(): boolean {
    return this.client?.connected || false;
  }
}

export const chatService = new ChatService();