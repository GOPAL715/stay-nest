/**
 * Token storage helpers.
 *
 * Access tokens are kept in memory (module-level variable) to reduce XSS surface area.
 * Refresh tokens are stored in localStorage so they survive page refreshes.
 *
 * NOTE: For maximum security the refresh token should be stored in an
 * HttpOnly cookie set by the server. localStorage is used here as a pragmatic
 * choice for a local-dev assignment. Do NOT use localStorage for refresh tokens
 * in a production deployment.
 */

const REFRESH_TOKEN_KEY = 'staynest_refresh_token';

// ─── In-memory access token ───────────────────────────────────────────────────

let _accessToken: string | null = null;

/** Return the current in-memory access token, or null if not set. */
export function getAccessToken(): string | null {
  return _accessToken;
}

/** Store the access token in memory. */
export function setAccessToken(token: string): void {
  _accessToken = token;
}

/** Clear the in-memory access token. */
export function clearAccessToken(): void {
  _accessToken = null;
}

// ─── Persisted refresh token ─────────────────────────────────────────────────

/** Return the persisted refresh token, or null if not found. */
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

/** Persist the refresh token to localStorage. */
export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

/** Remove the persisted refresh token from localStorage. */
export function clearRefreshToken(): void {
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

// ─── Combined helpers ─────────────────────────────────────────────────────────

/**
 * Persist both tokens at once (called after a successful login or token refresh).
 */
export function setTokens(accessToken: string, refreshToken: string): void {
  setAccessToken(accessToken);
  setRefreshToken(refreshToken);
}

/**
 * Clear both tokens (called on logout or unrecoverable auth failure).
 */
export function clearTokens(): void {
  clearAccessToken();
  clearRefreshToken();
}

/** Return true if the user is currently authenticated (has an access token in memory). */
export function isAuthenticated(): boolean {
  return _accessToken !== null;
}
