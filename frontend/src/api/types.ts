export interface ApiPage<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface LoginResponse {
  token: string;
  username: string;
  roleCode: "USER" | "ADMIN";
}

export interface UserProfile {
  id: number;
  username: string;
  realName: string;
  phone: string;
  roleCode: "USER" | "ADMIN";
}

export interface CarInfo {
  id: number;
  brand: string;
  series?: string;
  model: string;
  plateNo: string;
  seatCount: number;
  gearbox?: string;
  fuelType?: string;
  dayRent: number;
  deposit: number;
  status: number;
  mileage: number;
  coverUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RentalOrder {
  id: number;
  orderNo: string;
  userId: number;
  carId: number;
  rentStartTime: string;
  rentEndTime: string;
  rentDays: number;
  dayRent: number;
  deposit: number;
  baseAmount: number;
  extraAmount: number;
  totalAmount: number;
  orderStatus: number;
  remark?: string;
  createdAt: string;
  updatedAt: string;
}

