/**
 * apiFetch — JWT-interceptor fetch wrapper.
 *
 * API base URL is read from VITE_API_BASE_URL in .env so no proxy is needed.
 * All relative /api paths are prefixed with the base URL automatically.
 */

import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
} from '../utils/tokenStorage';

// ─── Base URL ─────────────────────────────────────────────────────────────────

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL as string ?? '';

/** Resolve a path like /api/v1/auth/login → http://localhost:8081/api/v1/auth/login */
function resolveUrl(url: string): string {
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return `${API_BASE_URL}${url}`;
}

// ─── Types ────────────────────────────────────────────────────────────────────

interface RefreshResponse {
  success: boolean;
  data: {
    accessToken: string;
    refreshToken: string;
  };
}

// ─── Token refresh logic ──────────────────────────────────────────────────────

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  try {
    const response = await fetch(resolveUrl('/api/v1/auth/refresh'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      clearTokens();
      return null;
    }

    const json: RefreshResponse = await response.json() as RefreshResponse;
    if (!json.success || !json.data?.accessToken) {
      clearTokens();
      return null;
    }

    setTokens(json.data.accessToken, json.data.refreshToken);
    return json.data.accessToken;
  } catch {
    clearTokens();
    return null;
  }
}

// ─── Fetch wrapper ────────────────────────────────────────────────────────────

export async function apiFetch(
  url: string,
  options: RequestInit = {},
): Promise<Response> {
  const token = getAccessToken();
  const fullUrl = resolveUrl(url);

  const headers = new Headers(options.headers);
  headers.set('Content-Type', headers.get('Content-Type') ?? 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(fullUrl, { ...options, headers });

  if (response.status !== 401) {
    return response;
  }

  const newToken = await refreshAccessToken();
  if (!newToken) {
    // Refresh token is also expired — force re-login
    clearTokens();
    window.location.href = '/login';
    return response;
  }

  const retryHeaders = new Headers(options.headers);
  retryHeaders.set('Content-Type', retryHeaders.get('Content-Type') ?? 'application/json');
  retryHeaders.set('Authorization', `Bearer ${newToken}`);

  return fetch(fullUrl, { ...options, headers: retryHeaders });
}

// ─── Convenience helpers ──────────────────────────────────────────────────────

export async function apiGet<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await apiFetch(url, { ...options, method: 'GET' });
  if (!res.ok) throw new Error(`GET ${url} failed with status ${res.status}`);
  return res.json() as Promise<T>;
}

export async function apiPost<T>(url: string, body?: unknown, options?: RequestInit): Promise<T> {
  const res = await apiFetch(url, {
    ...options,
    method: 'POST',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`POST ${url} failed with status ${res.status}`);
  return res.json() as Promise<T>;
}

export async function apiPut<T>(url: string, body?: unknown, options?: RequestInit): Promise<T> {
  const res = await apiFetch(url, {
    ...options,
    method: 'PUT',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`PUT ${url} failed with status ${res.status}`);
  return res.json() as Promise<T>;
}

export async function apiPatch<T>(url: string, body?: unknown, options?: RequestInit): Promise<T> {
  const res = await apiFetch(url, {
    ...options,
    method: 'PATCH',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`PATCH ${url} failed with status ${res.status}`);
  return res.json() as Promise<T>;
}

export async function apiDelete<T = void>(url: string, options?: RequestInit): Promise<T> {
  const res = await apiFetch(url, { ...options, method: 'DELETE' });
  if (!res.ok) throw new Error(`DELETE ${url} failed with status ${res.status}`);
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}
