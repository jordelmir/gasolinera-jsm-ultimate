"use client"; // Muy importante

import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  user: any | null; // Puedes usar un tipo más específico para el usuario
  login: (token: string, user: any) => void;
  logout: () => void;
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
      user: null,
      login: (token, user) => set({ token, user }),
      logout: () => set({ token: null, user: null }),
    }),
    {
      name: 'auth-storage', // nombre de la clave en localStorage
      // Usa el localStorage real en el cliente, y el dummy en el servidor
      storage: typeof window !== 'undefined' ? createJSONStorage(() => localStorage) : dummyStorage,
    }
  )
);