// User Types
export interface User {
  userId: number;
  username: string;
  email: string;
  point: number;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

// Match Types
export const MatchStatus = {
  SCHEDULED: 'SCHEDULED',
  ONGOING: 'ONGOING',
  FINISHED: 'FINISHED'
} as const;

export type MatchStatus = typeof MatchStatus[keyof typeof MatchStatus];

export interface Match {
  matchId: number;
  fetchId: string;
  matchName: string;
  teamA: string;
  teamB: string;
  totalAmountA: number;
  totalAmountB: number;
  startTime: string;
  endTime?: string;
  status: MatchStatus;
  winner?: string;
  loser?: string;
  viewCount: number;
  createdAt: string;
}

export interface MatchSearchRequest {
  keyword?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

// Bet Types
export const SelectedTeam = {
  Team_A: 'Team_A',
  Team_B: 'Team_B'
} as const;

export type SelectedTeam = typeof SelectedTeam[keyof typeof SelectedTeam];

export interface MatchBetResponse {
  matchId: number;
  matchName: string;
  teamA: string;
  teamB: string;
  startTime: string;
  status: MatchStatus;
  winner?: string;
}

export interface Bet {
  betId: number;
  matchBetResponse: MatchBetResponse;
  selectedTeam: SelectedTeam;
  betAmount: number;
  oddsAtBetting: number;
  isWin: boolean;
  createdAt: string;
}

export interface BetRequest {
  matchId: number;
  selectedTeam: SelectedTeam;
  betAmount: number;
}

// Pagination
export interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  content: T[];
  pageInfo: PageInfo;
}

// API Response
export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  path: string;
}