import { api } from './api';
import type { AuthResponse, LoginRequest, SignupRequest, User, ApiResponse } from '../types';

export const authService = {
  // 회원가입 (토큰 반환 없음)
  signup: async (data: SignupRequest): Promise<void> => {
    await api.post('/api/v1/auth/signup', data);
  },

  // 로그인
  login: async (data: LoginRequest): Promise<User> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', data);
    const authData = response.data.data;

    // Access Token 저장
    localStorage.setItem('accessToken', authData.accessToken);

    // 사용자 정보 가져오기
    const user = await authService.getMe();
    return user;
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    await api.post('/api/v1/auth/logout');
    localStorage.removeItem('accessToken');
  },

  // 회원탈퇴
  withdraw: async (): Promise<void> => {
    await api.delete('/api/v1/auth/withdraw');
    localStorage.removeItem('accessToken');
  },

  // 내 정보 조회
  getMe: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>('/api/v1/users/me');
    return response.data.data;
  },

  // Access Token 확인
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('accessToken');
  },
};