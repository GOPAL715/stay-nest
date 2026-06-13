import { useEffect, useState } from 'react';
import { Link, useSearch } from '@tanstack/react-router';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';

export function VerifyEmailPage() {
  const { token } = useSearch({ from: '/verify-email' });
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Missing verification token.');
      return;
    }

    void (async () => {
      try {
        const res = await apiFetch(`/api/v1/auth/verify-email?token=${encodeURIComponent(token)}`);
        const json = (await res.json()) as ApiEnvelope<void>;
        if (!res.ok || !json.success) {
          setStatus('error');
          setMessage(json.message || 'Verification failed.');
          return;
        }
        setStatus('success');
        setMessage(json.message || 'Email verified successfully.');
      } catch {
        setStatus('error');
        setMessage('Verification failed. The link may have expired.');
      }
    })();
  }, [token]);

  return (
    <div className="mx-auto max-w-md px-4 py-16 text-center">
      {status === 'loading' && <PageLoader message="Verifying email…" />}
      {status === 'success' && (
        <>
          <h1 className="text-2xl font-bold text-gray-900">Email verified!</h1>
          <p className="mt-4 text-gray-600">{message}</p>
          <Link to="/login" className="btn-primary mt-8 inline-flex">Sign in</Link>
        </>
      )}
      {status === 'error' && (
        <>
          <h1 className="text-2xl font-bold text-gray-900">Verification failed</h1>
          <p className="mt-4 text-gray-600">{message}</p>
          <Link to="/register" className="btn-secondary mt-8 inline-flex">Register again</Link>
        </>
      )}
    </div>
  );
}
