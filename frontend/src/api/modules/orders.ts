import http from "@/api/http";
import type { ApiPage, RentalOrder } from "@/api/types";

export interface CreateOrderPayload {
  carId: number;
  rentStartTime: string;
  rentEndTime: string;
  remark?: string;
}

export function createOrder(payload: CreateOrderPayload) {
  return http.post<never, RentalOrder>("/orders", payload);
}

export function fetchMyOrders(params: { page?: number; size?: number }) {
  return http.get<never, ApiPage<RentalOrder>>("/orders/my", { params });
}

export function fetchOrder(id: number) {
  return http.get<never, RentalOrder>(`/orders/${id}`);
}

export function cancelOrder(id: number) {
  return http.put<never, void>(`/orders/${id}/cancel`);
}

export function payOrder(id: number) {
  return http.post<never, void>(`/orders/${id}/pay`);
}

export function adminFetchOrders(params: { page?: number; size?: number; status?: number }) {
  return http.get<never, ApiPage<RentalOrder>>("/admin/orders", { params });
}

export function adminPickupOrder(id: number) {
  return http.put<never, void>(`/admin/orders/${id}/pickup`);
}

export function adminReturnOrder(id: number, extraAmount: number) {
  return http.put<never, void>(`/admin/orders/${id}/return`, { extraAmount });
}

