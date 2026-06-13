import { apiFetch } from './apiFetch';
import type { ApiEnvelope, Payment } from '../types/api';
import { formatApiErrorMessage } from '../utils/apiError';

const DEMO_SIGNATURE = 'demo_signature';

async function parsePaymentResponse(res: Response): Promise<Payment> {
  const json = (await res.json()) as ApiEnvelope<Payment>;
  if (!res.ok || !json.success) {
    throw new Error(formatApiErrorMessage(json));
  }
  return json.data;
}

export async function fetchPaymentForBooking(bookingId: string): Promise<Payment | null> {
  const res = await apiFetch(`/api/v1/payments/bookings/${bookingId}`);
  const json = (await res.json()) as ApiEnvelope<Payment | null>;
  if (res.status === 404) return null;
  if (!res.ok || !json.success) {
    throw new Error(formatApiErrorMessage(json));
  }
  return json.data ?? null;
}

export async function createPaymentOrder(bookingId: string): Promise<Payment> {
  const res = await apiFetch('/api/v1/payments/orders', {
    method: 'POST',
    body: JSON.stringify({ bookingId }),
  });
  return parsePaymentResponse(res);
}

/** Simulates a successful Razorpay checkout — no external API or keys required. */
export async function completeDemoPayment(order: Payment): Promise<Payment> {
  const res = await apiFetch('/api/v1/payments/verify', {
    method: 'POST',
    body: JSON.stringify({
      razorpayOrderId: order.razorpayOrderId,
      razorpayPaymentId: `pay_demo_${order.razorpayOrderId}`,
      razorpaySignature: DEMO_SIGNATURE,
    }),
  });
  return parsePaymentResponse(res);
}
