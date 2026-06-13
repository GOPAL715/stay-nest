import { formatPaise } from '../utils/format';

interface DemoPaymentModalProps {
  open: boolean;
  title: string;
  amount: number;
  currency: string;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DemoPaymentModal({
  open,
  title,
  amount,
  currency,
  loading = false,
  onConfirm,
  onCancel,
}: DemoPaymentModalProps) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white shadow-xl" role="dialog" aria-modal="true">
        <div className="border-b border-gray-200 px-6 py-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-primary-600">Demo payment</p>
          <h3 className="mt-1 text-lg font-bold text-gray-900">Razorpay checkout (simulated)</h3>
          <p className="mt-1 text-sm text-gray-500">{title}</p>
        </div>

        <div className="space-y-4 px-6 py-5">
          <div className="rounded-xl bg-gray-50 p-4 text-center">
            <p className="text-sm text-gray-500">Amount to pay</p>
            <p className="mt-1 text-3xl font-bold text-gray-900">
              {currency === 'INR' ? formatPaise(amount) : `${amount} ${currency}`}
            </p>
          </div>

          <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
            This is a demo only. No real money is charged and no Razorpay account is needed.
          </div>

          <div className="rounded-lg border border-gray-200 p-3 text-xs text-gray-500">
            Test card (not used in demo): 4111 1111 1111 1111 · any future expiry · any CVV
          </div>
        </div>

        <div className="flex gap-3 border-t border-gray-200 px-6 py-4">
          <button type="button" className="btn-secondary flex-1" disabled={loading} onClick={onCancel}>
            Cancel
          </button>
          <button type="button" className="btn-primary flex-1" disabled={loading} onClick={onConfirm}>
            {loading ? 'Processing…' : 'Simulate successful payment'}
          </button>
        </div>
      </div>
    </div>
  );
}
