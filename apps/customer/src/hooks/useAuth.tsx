import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, AuthData, UserProfile } from '../types/api';
import {
  clearTokens,
  getRefreshToken,
  setTokens,
} from '../utils/tokenStorage';

interface AuthContextValue {
  user: UserProfile | null;
  /** @deprecated use isRestoring */
  loading: boolean;
  isRestoring: boolean;
  isLoggedIn: boolean;
  login: (email: string, password: string) => Promise<UserProfile>;
  register: (data: RegisterInput) => Promise<{ email: string }>;
  verifyOtp: (email: string, otp: string) => Promise<void>;
  logout: () => Promise<void>;
}

interface RegisterInput {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isRestoring, setIsRestoring] = useState(true);

  useEffect(() => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      setIsRestoring(false);
      return;
    }

    apiFetch('/api/v1/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (res) => {
        if (!res.ok) {
          clearTokens();
          return;
        }
        const json = (await res.json()) as ApiEnvelope<AuthData>;
        if (!json.success || !json.data) {
          clearTokens();
          return;
        }
        setTokens(json.data.accessToken, json.data.refreshToken);
        setUser(json.data.user);
      })
      .catch(() => clearTokens())
      .finally(() => setIsRestoring(false));
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const res = await apiFetch('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    const json = (await res.json()) as ApiEnvelope<AuthData>;
    if (!res.ok || !json.success) {
      throw new Error(json.message || 'Login failed');
    }
    setTokens(json.data.accessToken, json.data.refreshToken);
    setUser(json.data.user);
    return json.data.user;
  }, []);

  const register = useCallback(async (data: RegisterInput): Promise<{ email: string }> => {
    const res = await apiFetch('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    const json = (await res.json()) as ApiEnvelope<AuthData>;
    if (!res.ok || !json.success) {
      throw new Error(json.message || 'Registration failed');
    }
    return { email: data.email };
  }, []);

  const verifyOtp = useCallback(async (email: string, otp: string) => {
    const res = await apiFetch('/api/v1/auth/verify-otp', {
      method: 'POST',
      body: JSON.stringify({ email, otp }),
    });
    const json = (await res.json()) as ApiEnvelope<void>;
    if (!res.ok || !json.success) {
      throw new Error(json.message || 'OTP verification failed');
    }
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await apiFetch('/api/v1/auth/logout', {
          method: 'POST',
          body: JSON.stringify({ refreshToken }),
        });
      } catch {
        // ignore network errors on logout
      }
    }
    clearTokens();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading: isRestoring,
      isRestoring,
      isLoggedIn: !!user,
      login,
      register,
      verifyOtp,
      logout,
    }),
    [user, isRestoring, login, register, verifyOtp, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
