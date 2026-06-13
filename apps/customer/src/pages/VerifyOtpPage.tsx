import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearch } from '@tanstack/react-router';
import { useAuth } from '../hooks/useAuth';

const OTP_LENGTH = 6;
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL as string ?? '';

export function VerifyOtpPage() {
  const { email } = useSearch({ from: '/verify-otp' });
  const navigate = useNavigate();
  const { verifyOtp } = useAuth();

  const [digits, setDigits] = useState<string[]>(Array(OTP_LENGTH).fill(''));
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);

  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Focus first box on mount
  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  // Resend cooldown timer
  useEffect(() => {
    if (resendCooldown <= 0) return;
    const timer = setTimeout(() => setResendCooldown((c) => c - 1), 1000);
    return () => clearTimeout(timer);
  }, [resendCooldown]);

  // Redirect to register if no email provided
  if (!email) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Session expired</h1>
        <p className="mt-4 text-gray-500">Please register again to receive a new code.</p>
        <Link to="/register" className="btn-primary mt-6 inline-flex">Back to sign up</Link>
      </div>
    );
  }

  const handleChange = (index: number, value: string) => {
    // Accept only single digit
    const digit = value.replace(/\D/g, '').slice(-1);
    const updated = [...digits];
    updated[index] = digit;
    setDigits(updated);
    setError(null);

    // Auto-advance to next box
    if (digit && index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }

    // Auto-submit when all filled
    if (digit && index === OTP_LENGTH - 1) {
      const allFilled = updated.every((d) => d !== '');
      if (allFilled) {
        void submitOtp(updated.join(''));
      }
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (digits[index]) {
        // Clear current
        const updated = [...digits];
        updated[index] = '';
        setDigits(updated);
      } else if (index > 0) {
        // Move back
        inputRefs.current[index - 1]?.focus();
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, OTP_LENGTH);
    if (!pasted) return;
    const updated = [...digits];
    for (let i = 0; i < pasted.length; i++) {
      updated[i] = pasted[i];
    }
    setDigits(updated);
    // Focus last filled or last box
    const focusIndex = Math.min(pasted.length, OTP_LENGTH - 1);
    inputRefs.current[focusIndex]?.focus();

    if (pasted.length === OTP_LENGTH) {
      void submitOtp(pasted);
    }
  };

  const submitOtp = async (otp: string) => {
    if (isSubmitting) return;
    setIsSubmitting(true);
    setError(null);
    try {
      await verifyOtp(email, otp);
      void navigate({ to: '/login', search: { verified: 'true' } });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Verification failed. Please try again.');
      // Clear all boxes on error so user can retype
      setDigits(Array(OTP_LENGTH).fill(''));
      inputRefs.current[0]?.focus();
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const otp = digits.join('');
    if (otp.length < OTP_LENGTH) {
      setError('Please enter all 6 digits.');
      return;
    }
    void submitOtp(otp);
  };

  const handleResend = async () => {
    if (resendCooldown > 0) return;
    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/auth/resend-otp?email=${encodeURIComponent(email)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (!res.ok) {
        const json = await res.json() as { message?: string };
        throw new Error(json.message ?? 'Failed to resend');
      }
      setResendCooldown(60);
      setDigits(Array(OTP_LENGTH).fill(''));
      inputRefs.current[0]?.focus();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to resend code. Please try again.');
    }
  };

  const maskedEmail = email.replace(/(.{2}).+(@.+)/, '$1***$2');

  return (
    <div className="mx-auto max-w-md px-4 py-16 sm:px-6">
      {/* Header */}
      <div className="text-center">
        <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-primary-50">
          <span className="text-3xl">✉️</span>
        </div>
        <h1 className="text-2xl font-bold text-gray-900">Check your inbox</h1>
        <p className="mt-2 text-gray-500">
          We sent a 6-digit verification code to
        </p>
        <p className="font-medium text-gray-800">{maskedEmail}</p>
        <p className="mt-1 text-sm text-gray-400">Code expires in 10 minutes</p>
      </div>

      {/* OTP form */}
      <form onSubmit={handleSubmit} className="mt-8">
        <div
          className="flex justify-center gap-3"
          onPaste={handlePaste}
          aria-label="Enter verification code"
        >
          {digits.map((digit, index) => (
            <input
              key={index}
              ref={(el) => { inputRefs.current[index] = el; }}
              type="text"
              inputMode="numeric"
              pattern="\d*"
              maxLength={1}
              value={digit}
              onChange={(e) => handleChange(index, e.target.value)}
              onKeyDown={(e) => handleKeyDown(index, e)}
              aria-label={`Digit ${index + 1}`}
              className={[
                'h-14 w-12 rounded-xl border-2 text-center text-xl font-bold transition-all outline-none',
                'focus:border-primary-500 focus:ring-2 focus:ring-primary-200',
                digit ? 'border-primary-400 bg-primary-50 text-primary-700' : 'border-gray-300 bg-white text-gray-900',
                error ? 'border-red-400 bg-red-50' : '',
              ].join(' ')}
            />
          ))}
        </div>

        {/* Error */}
        {error && (
          <p className="mt-4 text-center text-sm text-red-600" role="alert">{error}</p>
        )}

        {/* Submit button */}
        <button
          type="submit"
          disabled={isSubmitting || digits.join('').length < OTP_LENGTH}
          className="btn-primary mt-6 w-full"
        >
          {isSubmitting ? 'Verifying…' : 'Verify email'}
        </button>
      </form>

      {/* Resend */}
      <div className="mt-6 text-center text-sm text-gray-500">
        Didn't receive the code?{' '}
        {resendCooldown > 0 ? (
          <span className="text-gray-400">Resend in {resendCooldown}s</span>
        ) : (
          <button
            type="button"
            onClick={() => void handleResend()}
            className="font-medium text-primary-600 hover:text-primary-700"
          >
            Resend code
          </button>
        )}
      </div>

      {/* Back to register */}
      <p className="mt-3 text-center text-sm text-gray-400">
        Wrong email?{' '}
        <Link to="/register" className="font-medium text-primary-600 hover:text-primary-700">
          Sign up again
        </Link>
      </p>
    </div>
  );
}
