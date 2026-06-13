import { useState } from 'react';
import { useNavigate } from '@tanstack/react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { apiFetch } from '../services/apiFetch';

const ADMIN_ROLES = [
  { value: 'SUPER_ADMIN',       label: 'Super Admin' },
  { value: 'PROPERTY_MANAGER',  label: 'Property Manager' },
  { value: 'SUPPORT_AGENT',     label: 'Support Agent' },
] as const;

type AdminRole = typeof ADMIN_ROLES[number]['value'];

const schema = z
  .object({
    firstName: z.string().min(1, 'First name is required').max(100),
    lastName: z.string().min(1, 'Last name is required').max(100),
    email: z.string().email('Must be a valid email address'),
    phone: z.string().max(20).optional().or(z.literal('')),
    role: z.enum(['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'], {
      required_error: 'Role is required',
    }),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(1, 'Please confirm the password'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type FormData = z.infer<typeof schema>;

interface ApiError {
  message?: string;
  errors?: { field: string; message: string }[];
}

export function CreateAdminPage() {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'SUPPORT_AGENT' },
  });

  const onSubmit = async (data: FormData) => {
    setServerError(null);

    const payload = {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      password: data.password,
      role: data.role as AdminRole,
      ...(data.phone ? { phone: data.phone } : {}),
    };

    try {
      // POST to the protected admin endpoint — apiFetch attaches the Bearer token automatically
      const res = await apiFetch('/api/v1/admin/users', {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      if (res.status === 201) {
        setSuccess(true);
        reset();
        return;
      }

      const body = await res.json() as ApiError;

      if (res.status === 401) {
        setServerError('Your session has expired. Please log in again.');
        return;
      }

      if (res.status === 403) {
        setServerError('Only Super Admins can create admin accounts.');
        return;
      }

      if (res.status === 409 || (res.status === 400 && body.message?.includes('already exists'))) {
        setServerError('An account with this email already exists.');
        return;
      }

      if (res.status === 400 && body.errors?.length) {
        setServerError(body.errors.map((e) => e.message).join(' · '));
        return;
      }

      setServerError(body.message ?? 'Failed to create admin. Please try again.');
    } catch {
      setServerError('Network error. Please check your connection.');
    }
  };

  if (success) {
    return (
      <div className="mx-auto max-w-lg">
        <div className="rounded-xl border border-green-200 bg-green-50 p-8 text-center">
          <div className="mb-3 text-4xl">✅</div>
          <h2 className="text-lg font-semibold text-green-800">Admin account created</h2>
          <p className="mt-1 text-sm text-green-700">
            The new admin can log in immediately — no email verification required.
          </p>
          <div className="mt-6 flex justify-center gap-3">
            <button
              type="button"
              onClick={() => setSuccess(false)}
              className="btn-primary"
            >
              Create another
            </button>
            <button
              type="button"
              onClick={() => void navigate({ to: '/users' })}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Go to Users
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Create Admin User</h1>
        <p className="mt-1 text-sm text-gray-500">
          Create a new admin account. Requires Super Admin privileges.
        </p>
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-8 shadow-sm">
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-5">

          {/* Name row */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="firstName" className="label">
                First name <span className="text-red-500">*</span>
              </label>
              <input
                id="firstName"
                type="text"
                autoComplete="given-name"
                className="input"
                {...register('firstName')}
              />
              {errors.firstName && (
                <p className="mt-1 text-xs text-red-600">{errors.firstName.message}</p>
              )}
            </div>
            <div>
              <label htmlFor="lastName" className="label">
                Last name <span className="text-red-500">*</span>
              </label>
              <input
                id="lastName"
                type="text"
                autoComplete="family-name"
                className="input"
                {...register('lastName')}
              />
              {errors.lastName && (
                <p className="mt-1 text-xs text-red-600">{errors.lastName.message}</p>
              )}
            </div>
          </div>

          {/* Email */}
          <div>
            <label htmlFor="email" className="label">
              Email address <span className="text-red-500">*</span>
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              className="input"
              placeholder="admin@staynest.com"
              {...register('email')}
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>
            )}
          </div>

          {/* Role */}
          <div>
            <label htmlFor="role" className="label">
              Role <span className="text-red-500">*</span>
            </label>
            <select id="role" className="input" {...register('role')}>
              {ADMIN_ROLES.map((r) => (
                <option key={r.value} value={r.value}>
                  {r.label}
                </option>
              ))}
            </select>
            {errors.role && (
              <p className="mt-1 text-xs text-red-600">{errors.role.message}</p>
            )}
          </div>

          {/* Phone */}
          <div>
            <label htmlFor="phone" className="label">
              Phone
              <span className="ml-1 text-xs text-gray-400">(optional)</span>
            </label>
            <input
              id="phone"
              type="tel"
              autoComplete="tel"
              className="input"
              placeholder="+919876543210"
              {...register('phone')}
            />
            {errors.phone && (
              <p className="mt-1 text-xs text-red-600">{errors.phone.message}</p>
            )}
          </div>

          {/* Password */}
          <div>
            <label htmlFor="password" className="label">
              Password <span className="text-red-500">*</span>
            </label>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              className="input"
              placeholder="Min. 8 characters"
              {...register('password')}
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-600">{errors.password.message}</p>
            )}
          </div>

          {/* Confirm Password */}
          <div>
            <label htmlFor="confirmPassword" className="label">
              Confirm password <span className="text-red-500">*</span>
            </label>
            <input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              className="input"
              {...register('confirmPassword')}
            />
            {errors.confirmPassword && (
              <p className="mt-1 text-xs text-red-600">{errors.confirmPassword.message}</p>
            )}
          </div>

          {/* Server error */}
          {serverError && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {serverError}
            </div>
          )}

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={() => void navigate({ to: '/users' })}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button type="submit" disabled={isSubmitting} className="btn-primary">
              {isSubmitting ? 'Creating…' : 'Create admin'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
