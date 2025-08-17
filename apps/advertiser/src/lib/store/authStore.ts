
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  setAuth: (token: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      setAuth: (token) => set({ token }),
      clearAuth: () => set({ token: null }),
    }),
    {
      name: 'advertiser-auth-storage',
      storage: createJSONStorage(() => localStorage),
    }
  )
);
