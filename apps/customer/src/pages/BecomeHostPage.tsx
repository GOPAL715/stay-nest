import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '../hooks/useAuth';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';
import { Link } from '@tanstack/react-router';

const schema = z.object({
  motivation: z.string().min(20, 'Please write at least 20 characters about your hosting motivation.'),
});

type FormData = z.infer<typeof schema>;

export function BecomeHostPage() {
  const { user } = useAuth();
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { isSubmitting, errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setError(null);
    try {
      const res = await apiFetch('/api/v1/host-applications', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      });
      const json = (await res.json()) as ApiEnvelope<any>;
      if (!res.ok || !json.success) {
        throw new Error(json.message || 'Submission failed');
      }
      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Submission failed');
    }
  };

  if (user?.role === 'HOST') {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Already a Host!</h1>
        <p className="mt-4 text-gray-600">
          You have already upgraded your role to a Host. You can access the Host features or manage listings.
        </p>
        <Link to="/" className="btn-primary mt-8 inline-flex">Back to Home</Link>
      </div>
    );
  }

  if (success) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-green-600">Application Submitted! 🎉</h1>
        <p className="mt-4 text-gray-600">
          Your host application has been successfully sent to the StayNest administration team for review. You will be notified via email once approved.
        </p>
        <Link to="/" className="btn-primary mt-8 inline-flex">Back to Home</Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16 sm:px-6">
      <h1 className="text-3xl font-bold tracking-tight text-gray-900">Become a Host</h1>
      <p className="mt-2 text-sm text-gray-500">
        Earn extra income by sharing your space. Tell us why you want to become a host on StayNest.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
        <div>
          <label htmlFor="motivation" className="label font-medium">Why do you want to become a host?</label>
          <textarea
            id="motivation"
            rows={5}
            placeholder="Introduce yourself, your properties, and why you would make a great host..."
            className="input w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500"
            {...register('motivation')}
          />
          {errors.motivation && <p className="mt-1 text-sm text-red-600">{errors.motivation.message}</p>}
        </div>

        {error && <p className="text-sm text-red-600 font-medium">{error}</p>}

        <button type="submit" disabled={isSubmitting} className="btn-primary w-full py-3">
          {isSubmitting ? 'Submitting Application…' : 'Submit Application'}
        </button>
      </form>
    </div>
  );
}
