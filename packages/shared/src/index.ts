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
