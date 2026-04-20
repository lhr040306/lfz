import http from "@/api/http";
import type { CarRevenueRank, RevenueTrendPoint, StatsOverview } from "@/api/types";

export interface StatsQuery {
  startDate?: string;
  endDate?: string;
}

export function adminFetchStatsOverview(params: StatsQuery) {
  return http.get<never, StatsOverview>("/admin/stats/overview", { params });
}

export function adminFetchRevenueTrend(params: StatsQuery) {
  return http.get<never, RevenueTrendPoint[]>("/admin/stats/revenue-trend", { params });
}

export function adminFetchCarRank(params: StatsQuery & { limit?: number }) {
  return http.get<never, CarRevenueRank[]>("/admin/stats/car-rank", { params });
}

