"use client"; // Muy importante

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface User {
  id: string;
  email: string;
  // Add other properties as they become known or are used in the application
  // e.g., name?: string; roles?: string[];
}

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: User | null; // Use the specific User type
  login: (token: string, user: User, refreshToken: string) => void;
  logout: () => void;
  setTokens: (token: string, refreshToken: string) => void; // New action
}

// Este es un truco para evitar errores de SSR con persist
// Crea un almacenamiento "dummy" en el servidor que no hace nada
const dummyStorage = createJSONStorage(() => ({
  getItem: () => null,
  setItem: () => {},
  removeItem: () => {},
}));

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      refreshToken: null,
      user: null,
      login: (token, user, refreshToken) => set({ token, user, refreshToken }),
      logout: () => set({ token: null, refreshToken: null, user: null }),
      setTokens: (token, refreshToken) => set({ token, refreshToken }),
    }),
    {
      name: 'auth-storage', // nombre de la clave en localStorage
      // Usa el localStorage real en el cliente, y el dummy en el servidor
      storage: typeof window !== 'undefined' ? createJSONStorage(() => localStorage) : dummyStorage,
    }
  )
);