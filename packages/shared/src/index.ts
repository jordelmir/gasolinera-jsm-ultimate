// This file can export shared interfaces, DTOs, validation schemas, etc.

export interface User {
  id: string;
  phone: string;
  roles: string[];
}

export interface QrPayload {
  s: string; // stationId
  d: string; // dispenserId
  n: string; // nonce
  t: number; // timestamp
  exp: number; // expiration
}

export interface Product {
  id: string;
  name: string;
  price: number;
  unit: string;
}

export interface Order {
  id: string;
  userId: string;
  products: Product[];
  totalAmount: number;
  status: 'pending' | 'completed' | 'cancelled';
  createdAt: number;
}

export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  error?: {
    message: string;
    code?: string;
  };
};
