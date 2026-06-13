import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, Booking, PageResult } from '../types/api';
import { formatApiErrorMessage, readApiErrorResponse } from '../utils/apiError';
import { formatDate, formatPaise } from '../utils/format';

export function HostBookingsPage() {
  const queryClient = useQueryClient();
  const [actionError, setActionError] = useState<string | null>(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['host-bookings'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/bookings/host-bookings?page=0&size=50');
      if (!res.ok) throw new Error('Failed to load booking requests');
      const json = (await res.json()) as ApiEnvelope<PageResult<Booking>>;
      return json.data;
    },
  });

  const confirmBooking = async (bookingId: string) => {
    setActionError(null);
    const res = await apiFetch(`/api/v1/bookings/${bookingId}/confirm`, { method: 'PATCH' });
    const body = await readApiErrorResponse(res);
    if (!res.ok || !body.success) {
      setActionError(formatApiErrorMessage(body));
      return;
    }
    void queryClient.invalidateQueries({ queryKey: ['host-bookings'] });
  };

  if (isLoading) {
    return <PageLoader message="Loading booking requests…" />;
  }

  const pending = data?.content.filter((b) => b.status === 'PENDING') ?? [];
  const other = data?.content.filter((b) => b.status !== 'PENDING') ?? [];

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      <h1 className="text-2xl font-bold text-gray-900">Guest Booking Requests</h1>
      <p className="mt-1 text-gray-500">
        Approve request-to-book reservations. Guests can pay after you confirm.
      </p>

      {actionError && (
        <div className="mt-4 rounded-md bg-red-50 p-4 text-sm text-red-600">{actionError}</div>
      )}

      {isError && <p className="mt-8 text-center text-red-600">Could not load booking requests.</p>}

      {!isError && pending.length === 0 && other.length === 0 && (
        <div className="mt-12 rounded-lg border-2 border-dashed border-gray-300 p-12 text-center text-gray-500">
          No booking requests yet.
        </div>
      )}

      {pending.length > 0 && (
        <section className="mt-8">
          <h2 className="text-lg font-semibold text-gray-900">Pending approval</h2>
          <ul className="mt-4 space-y-4">
            {pending.map((booking) => (
              <li key={booking.id} className="rounded-xl border border-yellow-200 bg-yellow-50/40 p-5">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <h3 className="font-semibold text-gray-900">{booking.propertyTitle}</h3>
                    <p className="text-sm text-gray-600">
                      {booking.guestFirstName} {booking.guestLastName} · {booking.numGuests} guests
                    </p>
                    <p className="mt-1 text-sm text-gray-600">
                      {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}
                    </p>
                    {booking.specialRequests && (
                      <p className="mt-2 text-sm text-gray-500">Note: {booking.specialRequests}</p>
                    )}
                  </div>
                  <div className="text-right">
                    {booking.priceBreakdown && (
                      <p className="font-semibold text-gray-900">
                        {booking.priceBreakdown.totalAmountInr
                          ? `₹${booking.priceBreakdown.totalAmountInr}`
                          : formatPaise(booking.priceBreakdown.totalAmount)}
                      </p>
                    )}
                    <button
                      type="button"
                      className="btn-primary mt-3"
                      onClick={() => void confirmBooking(booking.id)}
                    >
                      Approve booking
                    </button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      {other.length > 0 && (
        <section className="mt-10">
          <h2 className="text-lg font-semibold text-gray-900">Other bookings</h2>
          <ul className="mt-4 space-y-3">
            {other.map((booking) => (
              <li key={booking.id} className="rounded-xl border border-gray-200 bg-white p-4 text-sm">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <span className="font-medium text-gray-900">{booking.propertyTitle}</span>
                  <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-700">
                    {booking.status}
                  </span>
                </div>
                <p className="mt-1 text-gray-500">
                  {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}
                </p>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  );
}
