import { useAuthStore } from "./store/authStore";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';

async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const token = useAuthStore.getState().token;
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, { ...options, headers });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: `Error: ${response.status}` }));
    throw new Error(errorBody.message);
  }
  return response.status === 204 ? null : response.json();
}

// --- Auth --- //
export const loginAdvertiser = (email: string, pass: string) => {
  return apiFetch('/auth/login/advertiser', {
    method: 'POST',
    body: JSON.stringify({ email, pass }),
  });
};

// --- Campaigns --- //
export interface Campaign {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  budget: number;
  adUrl: string;
}

export const getMyCampaigns = (): Promise<Campaign[]> => apiFetch('/campaigns');

export const createCampaign = (campaignData: Omit<Campaign, 'id'>): Promise<Campaign> => {
  return apiFetch('/campaigns', {
    method: 'POST',
    body: JSON.stringify(campaignData),
  });
};

export const updateCampaign = (id: number, campaignData: Partial<Omit<Campaign, 'id'>>): Promise<Campaign> => {
  return apiFetch(`/campaigns/${id}`, {
    method: 'PUT',
    body: JSON.stringify(campaignData),
  });
};

export const deleteCampaign = (id: number): Promise<void> => {
  return apiFetch(`/campaigns/${id}`, {
    method: 'DELETE',
  });
};

export const getCampaignPerformanceSummary = (): Promise<{ totalImpressions: number; totalBudgetSpent: number }> => {
  return apiFetch('/campaigns/summary');
};