import axios from 'axios';

const API_URL = 'http://localhost:8080'; // API Gateway URL

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const redeemQrCode = async (qr_token: string, gps: string) => {
  const response = await apiClient.post('/redemption-service/redeem', { qr_token, gps });
  return response.data;
};

export const confirmAdWatched = async (session_id: string) => {
  const response = await apiClient.post('/redemption-service/redeem/confirm-ad', { session_id });
  return response.data;
};
