import { useState } from 'react';
import { Link } from '@tanstack/react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
});

type FormData = z.infer<typeof schema>;

export function ForgotPasswordPage() {
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { isSubmitting, errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setError(null);
    try {
      const res = await apiFetch('/api/v1/auth/forgot-password', {
        method: 'POST',
        body: JSON.stringify(data),
      });
      const json = (await res.json()) as ApiEnvelope<void>;
      if (!res.ok || !json.success) throw new Error(json.message || 'Request failed');
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    }
  };

  if (success) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Check your email</h1>
        <p className="mt-4 text-gray-600">
          If an account exists with that email, a password reset link has been sent.
        </p>
        <Link to="/login" className="btn-primary mt-8 inline-flex">Back to login</Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16 sm:px-6">
      <h1 className="text-2xl font-bold text-gray-900">Forgot password</h1>
      <p className="mt-2 text-gray-500">Enter your email and we&apos;ll send a reset link.</p>
      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-4">
        <div>
          <label htmlFor="email" className="label">Email</label>
          <input id="email" type="email" className="input" {...register('email')} />
          {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button type="submit" disabled={isSubmitting} className="btn-primary w-full">
          {isSubmitting ? 'Sending…' : 'Send reset link'}
        </button>
      </form>
      <p className="mt-6 text-center text-sm">
        <Link to="/login" className="text-primary-600 hover:text-primary-700">Back to login</Link>
      </p>
    </div>
  );
}
