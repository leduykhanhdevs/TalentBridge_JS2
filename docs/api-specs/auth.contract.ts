/**
 * TalentBridge Authentication API Contract for Frontend (Next.js / React)
 * Khớp 100% với DTOs của Backend Spring Boot
 */

export interface ApiResponse<T> {
  statusCode: number;
  message: string;
  data: T;
  timestamp?: string;
}

export interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  phone?: string;
  avatarUrl?: string;
  status: 'ACTIVE' | 'BANNED';
  roles: ('ROLE_CANDIDATE' | 'ROLE_RECRUITER' | 'ROLE_ADMIN')[];
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  user: UserResponse;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
  role: 'ROLE_CANDIDATE' | 'ROLE_RECRUITER';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}
