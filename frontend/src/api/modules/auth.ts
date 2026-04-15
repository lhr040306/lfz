import http from "@/api/http";
import type { LoginResponse, UserProfile } from "@/api/types";

export interface RegisterPayload {
  username: string;
  password: string;
  phone?: string;
  realName: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export function register(payload: RegisterPayload) {
  return http.post<never, void>("/auth/register", payload);
}

export function login(payload: LoginPayload) {
  return http.post<never, LoginResponse>("/auth/login", payload);
}

export function fetchProfile() {
  return http.get<never, UserProfile>("/auth/profile");
}

