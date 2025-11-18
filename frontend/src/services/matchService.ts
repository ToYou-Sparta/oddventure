import { api } from './api';
import type { Match, MatchSearchRequest, PageResponse, ApiResponse } from '../types';

export const matchService = {
  // 경기 목록 조회
  getMatches: async (page = 0, size = 20): Promise<PageResponse<Match>> => {
    const response = await api.get<ApiResponse<PageResponse<Match>>>('/api/v1/matches', {
      params: { page, size },
    });
    return response.data.data;
  },

  // 경기 상세 조회
  getMatch: async (matchId: number): Promise<Match> => {
    const response = await api.get<ApiResponse<Match>>(`/api/v1/matches/${matchId}`);
    return response.data.data;
  },

  // 경기 검색 (MySQL)
  searchMatches: async (searchParams: MatchSearchRequest): Promise<PageResponse<Match>> => {
    const { page = 0, size = 20, ...condition } = searchParams;
    const response = await api.post<ApiResponse<PageResponse<Match>>>(
      '/api/v1/matches/search',
      condition,
      { params: { page, size } }
    );
    return response.data.data;
  },

  // 경기 검색 (Elasticsearch)
  searchMatchesES: async (searchParams: MatchSearchRequest): Promise<PageResponse<Match>> => {
    const { page = 0, size = 20, ...condition } = searchParams;
    const response = await api.post<ApiResponse<PageResponse<Match>>>(
      '/api/v1/matches/v2/search',
      condition,
      { params: { page, size } }
    );
    return response.data.data;
  },
};