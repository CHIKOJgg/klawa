import { create } from 'zustand';
import { api } from './api';

interface AuthState {
  token: string | null;
  user: any | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (firstName: string, lastName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  loadUser: () => Promise<void>;
}

export const useAuth = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  user: null,
  loading: false,
  login: async (email, password) => {
    const res = await api.auth.login({ email, password });
    localStorage.setItem('token', res.token);
    set({ token: res.token });
  },
  register: async (firstName, lastName, email, password) => {
    await api.auth.register({ firstName, lastName, email, password });
  },
  logout: () => {
    localStorage.removeItem('token');
    set({ token: null, user: null });
  },
  loadUser: async () => {
    try {
      const user = await api.auth.me();
      set({ user });
    } catch {}
  },
}));
