import { create } from 'zustand';

interface UserProfile {
  name: string;
  email: string;
  authority: string; // 추가: 사용자 권한 (예: 'ROLE_ADMIN', 'ROLE_USER')
}

interface UserState {
  user: UserProfile;
  setUser: (user: UserProfile) => void;
  deleteUser: () => void;
}

const storedUser =
  typeof window !== 'undefined' ? JSON.parse(sessionStorage.getItem('user') || 'null') : null;

export const defaultState: UserProfile = {
  name: '게스트',
  email: '',
  authority: 'ROLE_USER' // 기본 권한은 'ROLE_USER'
};

export const useUserStore = create<UserState>((set) => ({
  user: storedUser || defaultState,
  setUser: (user: UserProfile) => {
    set({ user });
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('user', JSON.stringify(user));
    }
  },
  deleteUser: () => {
    set({ user: defaultState });
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('user');
    }
  },
}));
