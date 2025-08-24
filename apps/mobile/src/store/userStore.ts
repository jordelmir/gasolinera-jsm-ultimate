import { create } from 'zustand';

interface UserState {
  accessToken: string | null;
  refreshToken: string | null;
  points: number;
  setTokens: (access: string, refresh: string) => void;
  setPoints: (points: number) => void;
}

export const useUserStore = create<UserState>((set) => ({
  accessToken: null,
  refreshToken: null,
  points: 0,
  setTokens: (access, refresh) => set({ accessToken: access, refreshToken: refresh }),
  setPoints: (points) => set({ points }),
}));
