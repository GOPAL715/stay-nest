import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, AuthData, UserProfile } from '../types/api';
import { clearTokens, getRefreshToken, setTokens } from '../utils/tokenStorage';

interface AuthContextValue {
  user: UserProfile | null;
  isLoggedIn: boolean;
  isRestoring: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isRestoring, setIsRestoring] = useState(true);

  // On mount: silently restore the session using the persisted refresh token.
  // This fixes the 401 caused by the access token being lost on page reload
  // (access tokens are in-memory only; refresh tokens survive in localStorage).
  useEffect(() => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      setIsRestoring(false);
      return;
    }

    const allowedRoles = ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'];

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
        if (json.success && json.data?.accessToken && json.data.user && allowedRoles.includes(json.data.user.role)) {
          setTokens(json.data.accessToken, json.data.refreshToken);
          setUser(json.data.user);
        } else {
          clearTokens();
        }
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
    const allowedRoles = ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'];
    if (!json.data.user || !allowedRoles.includes(json.data.user.role)) {
      throw new Error('Access denied. Admin portal is restricted to authorized staff only.');
    }
    setTokens(json.data.accessToken, json.data.refreshToken);
    setUser(json.data.user);
  }, []);

  const logout = useCallback(async () => {
    clearTokens();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, isLoggedIn: !!user, isRestoring, login, logout }),
    [user, isRestoring, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
