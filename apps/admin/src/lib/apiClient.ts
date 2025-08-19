
import { useAuthStore } from "@/store/authStore"; // Assuming a zustand store for auth

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api/v1';

let isRefreshing = false;
let failedQueue: { resolve: (value: unknown) => void; reject: (reason?: any) => void; }[] = [];

const processQueue = (error: any | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(true);
    }
  });
  failedQueue = [];
};

const callRefreshToken = async () => {
  const { refreshToken, setTokens, logout } = useAuthStore.getState();
  if (!refreshToken) {
    logout();
    throw new Error("No refresh token available.");
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh-token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      logout();
      throw new Error("Failed to refresh token.");
    }

    const data = await response.json();
    setTokens(data.token, data.refreshToken); // Assuming setTokens updates both
    return data.token;
  } catch (error) {
    logout();
    throw error;
  }
};

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

  let response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  // Handle 401 Unauthorized
  if (response.status === 401 && !options._retry) { // _retry flag to prevent infinite loops
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
      .then(() => {
        // Retry the original request with the new token
        return apiFetch(endpoint, { ...options, _retry: true });
      })
      .catch(err => {
        return Promise.reject(err);
      });
    }

    isRefreshing = true;

    try {
      const newToken = await callRefreshToken();
      // Update token in headers for the original request
      headers['Authorization'] = `Bearer ${newToken}`;
      // Retry the original request with the new token
      response = await fetch(`${API_BASE_URL}${endpoint}`, { ...options, headers, _retry: true });
      processQueue(null); // Resolve all pending requests
    } catch (err) {
      processQueue(err); // Reject all pending requests
      logout(); // Clear auth state and redirect to login
      throw err; // Re-throw the error
    } finally {
      isRefreshing = false;
    }
  }

  if (!response.ok) {
    let errorMessage = `Error: ${response.status}`;
    let errorBody: any = {};
    try {
      errorBody = await response.json();
      errorMessage = errorBody.message || errorMessage;
    } catch (e) {
      // If response is not JSON, use status text or a generic message
      errorMessage = response.statusText || `Error: ${response.status}`;
    }

    switch (response.status) {
      case 400:
        errorMessage = errorMessage || 'Bad Request: The server cannot process the request due to a client error.';
        break;
      case 401:
        errorMessage = errorMessage || 'Unauthorized: Authentication is required and has failed or has not yet been provided.';
        break;
      case 403:
        errorMessage = errorMessage || 'Forbidden: The server understood the request but refuses to authorize it.';
        break;
      case 404:
        errorMessage = errorMessage || 'Not Found: The requested resource could not be found.';
        break;
      case 500:
        errorMessage = errorMessage || 'Internal Server Error: The server encountered an unexpected condition.';
        break;
      case 502:
        errorMessage = errorMessage || 'Bad Gateway: The server, while acting as a gateway or proxy, received an invalid response from an upstream server.';
        break;
      case 503:
        errorMessage = errorMessage || 'Service Unavailable: The server is not ready to handle the request.';
        break;
      case 504:
        errorMessage = errorMessage || 'Gateway Timeout: The server, while acting as a gateway or proxy, did not receive a timely response from an upstream server.';
        break;
      default:
        errorMessage = errorMessage || `An unexpected error occurred (Status: ${response.status}).`;
    }

    const error = new Error(errorMessage) as any;
    error.status = response.status;
    error.data = errorBody;
    throw error;
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
