import { api } from './api';
import type { Bet, BetRequest, PageResponse, ApiResponse } from '../types';

export const betService = {
  // 배팅 생성
  createBet: async (data: BetRequest): Promise<Bet> => {
    const response = await api.post<ApiResponse<Bet>>('/api/v1/bets', data);
    return response.data.data;
  },

  // 내 배팅 내역 조회
  getMyBets: async (page = 0, size = 20): Promise<PageResponse<Bet>> => {
    const response = await api.get<ApiResponse<PageResponse<Bet>>>('/api/v1/bets/me', {
      params: { page, size },
    });
    return response.data.data;
  },

  // 배팅 취소
  cancelBet: async (betId: number): Promise<void> => {
    await api.delete(`/api/v1/bets/${betId}`);
  },
};