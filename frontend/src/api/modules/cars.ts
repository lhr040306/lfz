import http from "@/api/http";
import type { ApiPage, CarInfo } from "@/api/types";

export interface CarQuery {
  page?: number;
  size?: number;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
}

export interface CarPayload {
  brand: string;
  series?: string;
  model: string;
  plateNo: string;
  seatCount: number;
  gearbox?: string;
  fuelType?: string;
  dayRent: number;
  deposit: number;
  status?: number;
  mileage?: number;
  coverUrl?: string;
}

export function fetchCars(params: CarQuery) {
  return http.get<never, ApiPage<CarInfo>>("/cars", { params });
}

export function fetchCarDetail(id: number) {
  return http.get<never, CarInfo>(`/cars/${id}`);
}

export function adminFetchCars(params: { page?: number; size?: number; brand?: string; status?: number }) {
  return http.get<never, ApiPage<CarInfo>>("/admin/cars", { params });
}

export function adminCreateCar(payload: CarPayload) {
  return http.post<never, CarInfo>("/admin/cars", payload);
}

export function adminUpdateCar(id: number, payload: CarPayload) {
  return http.put<never, CarInfo>(`/admin/cars/${id}`, payload);
}

export function adminUpdateCarStatus(id: number, status: number) {
  return http.put<never, void>(`/admin/cars/${id}/status`, { status });
}

export function adminDeleteCar(id: number) {
  return http.delete<never, void>(`/admin/cars/${id}`);
}
