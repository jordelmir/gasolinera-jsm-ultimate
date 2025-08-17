
import { useAuthStore } from "@/store/authStore"; // Assuming a zustand store for auth

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';

/**
 * Performs a fetch request to the API, automatically adding the auth token.
 * @throws {Error} If the network response is not ok.
 */
async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const token = useAuthStore.getState().token;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = `Error: ${response.status}`;
    try {
      const errorBody = await response.json();
      errorMessage = errorBody.message || errorMessage;
    } catch (e) {
      // Ignore if error body is not JSON
    }
    throw new Error(errorMessage);
  }
  
  return response.status === 204 ? null : response.json();
}

// --- Authentication --- //
export const loginAdmin = (email: string, pass: string) => {
  return apiFetch('/auth/login/admin', {
    method: 'POST',
    body: JSON.stringify({ email, pass }),
  });
};

// --- Stations --- //
export type Station = {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  status: string;
};

export const getStations = (): Promise<Station[]> => {
  return apiFetch('/stations');
};

export const createStation = (stationData: Omit<Station, 'id' | 'status'>): Promise<Station> => {
  return apiFetch('/stations', {
    method: 'POST',
    body: JSON.stringify(stationData),
  });
};

export const updateStation = (stationId: string, stationData: Partial<Omit<Station, 'id'>>): Promise<Station> => {
  return apiFetch(`/stations/${stationId}`, {
    method: 'PUT',
    body: JSON.stringify(stationData),
  });
};

export const deleteStation = (stationId: string): Promise<void> => {
  return apiFetch(`/stations/${stationId}`, {
    method: 'DELETE',
  });
};

// --- Analytics --- //
export const getTodaySummary = () => {
    return apiFetch('/analytics/summary/today');
}

// --- Raffles --- //
export type Raffle = {
  id: number;
  period: string;
  merkleRoot: string;
  status: 'OPEN' | 'CLOSED' | 'DRAWN';
  drawAt?: string; // ISO date string
  externalSeed?: string;
  winnerEntryId?: string;
};

export type RaffleWinner = {
  id: number;
  raffleId: number;
  userId: string;
  winningPointId: string;
  prize: string;
};

export const getRaffles = (): Promise<Raffle[]> => {
  return apiFetch('/raffles');
};

export const closeRafflePeriod = (period: string): Promise<Raffle> => {
  return apiFetch(`/raffles/${period}/close`, {
    method: 'POST',
  });
};

export const executeRaffleDraw = (raffleId: number): Promise<RaffleWinner> => {
  return apiFetch(`/raffles/${raffleId}/draw`, {
    method: 'POST',
  });
};
