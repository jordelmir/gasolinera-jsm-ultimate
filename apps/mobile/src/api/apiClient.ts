// Asume que el token de autenticación se gestiona globalmente (ej. Zustand, Context)
let authToken: string | null = null;

// Función para establecer el token desde la app
export const setAuthToken = (token: string | null) => {
  authToken = token;
};

const API_BASE_URL = 'http://192.168.1.100:8080/api/v1'; // Reemplazar con la IP del API Gateway

/**
 * Realiza una petición fetch y maneja errores de forma centralizada.
 * @param endpoint El endpoint de la API al que llamar.
 * @param options Opciones de la petición fetch.
 * @returns La respuesta JSON.
 * @throws {Error} Si la respuesta de la red no es OK.
 */
async function apiFetch(endpoint: string, options: RequestInit = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = `Error: ${response.status} ${response.statusText}`;
    try {
      const errorBody = await response.json();
      errorMessage = errorBody.message || errorMessage;
    } catch (e) {
      // El cuerpo del error no era JSON, usar el mensaje de estado
    }
    throw new Error(errorMessage);
  }

  // Si la respuesta no tiene contenido (ej. 204 No Content), devolver null
  if (response.status === 204) {
    return null;
  }

  return response.json();
}

/**
 * Solicita un código OTP para un número de teléfono.
 */
export const requestOtp = (phone: string): Promise<void> => {
  return apiFetch('/auth/otp/request', {
    method: 'POST',
    body: JSON.stringify({ phone }),
  });
};

/**
 * Verifica un código OTP y devuelve un token de acceso.
 */
export const verifyOtp = (phone: string, code: string): Promise<{ accessToken: string }> => {
  return apiFetch('/auth/otp/verify', {
    method: 'POST',
    body: JSON.stringify({ phone, code }),
  });
};

/**
 * Envía un código QR para iniciar el proceso de redención.
 */
export const redeemQrCode = (qrCode: string): Promise<{ adUrl: string; redemptionId: string }> => {
  return apiFetch('/redemptions', {
    method: 'POST',
    body: JSON.stringify({ qrCode }),
  });
};

/**
 * Confirma que un anuncio ha sido visto.
 */
export const confirmAdWatched = (redemptionId: string): Promise<void> => {
  return apiFetch('/redemptions/confirm', {
    method: 'POST',
    body: JSON.stringify({ redemptionId }),
  });
};

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

export const getRaffleWinner = (raffleId: number): Promise<RaffleWinner> => {
  return apiFetch(`/raffles/${raffleId}/winner`);
};
