import { api } from './api';
import type { Match, User, PageResponse, ApiResponse } from '../types';

export const adminService = {
  // 경기 관리
  fetchMatches: async (): Promise<void> => {
    await api.post('/api/v1/admin/matches');
  },

  updateMatch: async (matchId: number, data: Partial<Match>): Promise<Match> => {
    const response = await api.patch<ApiResponse<Match>>(`/api/v1/admin/matches/${matchId}`, data);
    return response.data.data;
  },

  fetchMatchResult: async (fetchId: string): Promise<Match> => {
    const response = await api.patch<ApiResponse<Match>>(`/api/v1/admin/matches/fetch/${fetchId}`);
    return response.data.data;
  },

  syncElasticsearch: async (): Promise<void> => {
    await api.post('/api/v1/admin/matches/sync-elasticsearch');
  },

  // 사용자 관리
  getUsers: async (page = 0, size = 20): Promise<PageResponse<User>> => {
    const response = await api.get<ApiResponse<PageResponse<User>>>('/api/v1/admin/users', {
      params: { page, size },
    });
    return response.data.data;
  },

  adjustUserPoints: async (userId: number, points: number): Promise<User> => {
    const response = await api.post<ApiResponse<User>>(`/api/v1/admin/users/${userId}/points`, {
      amount: points,
      reason: '관리자 포인트 조정',
    });
    return response.data.data;
  },
};
