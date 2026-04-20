import http from "@/api/http";
import type { ApiPage, CustomerInfo } from "@/api/types";

export interface CustomerQuery {
  page?: number;
  size?: number;
  keyword?: string;
  status?: number;
}

export interface CustomerPayload {
  username: string;
  password?: string;
  realName: string;
  phone?: string;
  status?: number;
}

export function adminFetchCustomers(params: CustomerQuery) {
  return http.get<never, ApiPage<CustomerInfo>>("/admin/customers", { params });
}

export function adminFetchCustomerDetail(id: number) {
  return http.get<never, CustomerInfo>(`/admin/customers/${id}`);
}

export function adminCreateCustomer(payload: CustomerPayload) {
  return http.post<never, CustomerInfo>("/admin/customers", payload);
}

export function adminUpdateCustomer(id: number, payload: CustomerPayload) {
  return http.put<never, CustomerInfo>(`/admin/customers/${id}`, payload);
}

export function adminUpdateCustomerStatus(id: number, status: number) {
  return http.put<never, void>(`/admin/customers/${id}/status`, { status });
}

export function adminDeleteCustomer(id: number) {
  return http.delete<never, void>(`/admin/customers/${id}`);
}

