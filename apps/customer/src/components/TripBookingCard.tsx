import { useState } from 'react';
import { Link } from '@tanstack/react-router';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { DemoPaymentModal } from './DemoPaymentModal';
import type { Booking, Payment } from '../types/api';
import { formatDate, formatPaise } from '../utils/format';
import { completeDemoPayment, createPaymentOrder, fetchPaymentForBooking } from '../services/paymentService';

interface TripBookingCardProps {
  booking: Booking;
  detail?: boolean;
}

function statusHelpText(status: string, paymentStatus?: string): string | null {
  if (status === 'PENDING') {
    return 'Waiting for the host to approve your request. You can pay once it is confirmed.';
  }
  if (status === 'CONFIRMED' && paymentStatus !== 'PAID') {
    return 'Your booking is confirmed. Complete the demo payment to secure your stay.';
  }
  if (status === 'CONFIRMED' && paymentStatus === 'PAID') {
    return 'Demo payment received. Your trip is all set.';
  }
  return null;
}

export function TripBookingCard({ booking, detail = false }: TripBookingCardProps) {
  const queryClient = useQueryClient();
  const [payError, setPayError] = useState<string | null>(null);
  const [paying, setPaying] = useState(false);
  const [demoOrder, setDemoOrder] = useState<Payment | null>(null);

  const { data: payment, isLoading: paymentLoading } = useQuery({
    queryKey: ['payment', booking.id],
    enabled: booking.status === 'CONFIRMED',
    queryFn: () => fetchPaymentForBooking(booking.id),
    retry: false,
  });

  const totalLabel = booking.priceBreakdown?.totalAmountInr
    ? `₹${booking.priceBreakdown.totalAmountInr}`
    : booking.priceBreakdown
      ? formatPaise(booking.priceBreakdown.totalAmount)
      : null;

  const canPay =
    booking.status === 'CONFIRMED' &&
    payment?.status !== 'PAID' &&
    booking.priceBreakdown != null;

  const helpText = statusHelpText(booking.status, payment?.status);

  const handlePay = async () => {
    if (!booking.priceBreakdown) return;
    setPaying(true);
    setPayError(null);
    try {
      const order = await createPaymentOrder(booking.id);
      setDemoOrder(order);
    } catch (err) {
      setPayError(err instanceof Error ? err.message : 'Could not start payment');
    } finally {
      setPaying(false);
    }
  };

  const handleConfirmDemoPayment = async () => {
    if (!demoOrder) return;
    setPaying(true);
    setPayError(null);
    try {
      await completeDemoPayment(demoOrder);
      setDemoOrder(null);
      void queryClient.invalidateQueries({ queryKey: ['payment', booking.id] });
      void queryClient.invalidateQueries({ queryKey: ['my-trips'] });
      void queryClient.invalidateQueries({ queryKey: ['booking', booking.id] });
    } catch (err) {
      setPayError(err instanceof Error ? err.message : 'Payment failed');
    } finally {
      setPaying(false);
    }
  };

  const Wrapper = detail ? 'div' : 'li';

  return (
    <>
      <Wrapper
        className={[
          'rounded-xl border bg-white p-5 shadow-sm',
          detail ? 'border-gray-200' : 'border-gray-200 transition-shadow hover:shadow-md',
        ].join(' ')}
      >
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="min-w-0 flex-1">
            {detail ? (
              <h2 className="font-semibold text-gray-900">{booking.propertyTitle}</h2>
            ) : (
              <Link
                to="/trips/$bookingId"
                params={{ bookingId: booking.id }}
                className="font-semibold text-gray-900 hover:text-primary-600"
              >
                {booking.propertyTitle}
              </Link>
            )}
            <p className="text-sm text-gray-500">{booking.propertyCity}</p>
            <p className="mt-2 text-sm text-gray-600">
              {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)} · {booking.numGuests}{' '}
              guests
            </p>
            {detail && booking.hostFirstName && (
              <p className="mt-2 text-sm text-gray-600">
                Host: {booking.hostFirstName} {booking.hostLastName}
              </p>
            )}
            {detail && booking.specialRequests && (
              <p className="mt-2 text-sm text-gray-500">Requests: {booking.specialRequests}</p>
            )}
            {helpText && <p className="mt-2 text-sm text-gray-500">{helpText}</p>}
          </div>
          <div className="text-right">
            <span
              className={[
                'inline-block rounded-full px-3 py-1 text-xs font-medium',
                booking.status === 'CONFIRMED'
                  ? 'bg-green-100 text-green-800'
                  : booking.status === 'PENDING'
                    ? 'bg-yellow-100 text-yellow-800'
                    : booking.status === 'CANCELLED'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-primary-50 text-primary-700',
              ].join(' ')}
            >
              {booking.status}
            </span>
            {payment?.status === 'PAID' && (
              <span className="ml-2 inline-block rounded-full bg-green-50 px-3 py-1 text-xs font-medium text-green-700">
                Paid (demo)
              </span>
            )}
            {totalLabel && <p className="mt-2 font-semibold text-gray-900">{totalLabel}</p>}
          </div>
        </div>

        {detail && booking.priceBreakdown && (
          <dl className="mt-4 grid grid-cols-2 gap-2 border-t border-gray-100 pt-4 text-sm">
            <div>
              <dt className="text-gray-500">Nights</dt>
              <dd className="font-medium text-gray-900">{booking.priceBreakdown.numNights}</dd>
            </div>
            <div>
              <dt className="text-gray-500">Cleaning fee</dt>
              <dd className="font-medium text-gray-900">{formatPaise(booking.priceBreakdown.cleaningFee)}</dd>
            </div>
            <div>
              <dt className="text-gray-500">Policy</dt>
              <dd className="font-medium text-gray-900">{booking.cancellationPolicy}</dd>
            </div>
            <div>
              <dt className="text-gray-500">Booking ID</dt>
              <dd className="truncate font-mono text-xs text-gray-700">{booking.id}</dd>
            </div>
          </dl>
        )}

        <div className="mt-4 flex flex-wrap gap-2 border-t border-gray-100 pt-4">
          {!detail && (
            <Link
              to="/trips/$bookingId"
              params={{ bookingId: booking.id }}
              className="btn-secondary text-sm"
            >
              View trip
            </Link>
          )}
          <Link
            to="/properties/$propertyId"
            params={{ propertyId: booking.propertyId }}
            className="btn-secondary text-sm"
          >
            View property
          </Link>
          {canPay && (
            <button
              type="button"
              className="btn-primary text-sm"
              disabled={paying || paymentLoading}
              onClick={() => void handlePay()}
            >
              {paying ? 'Opening…' : `Pay ${totalLabel ?? 'now'} (demo)`}
            </button>
          )}
        </div>

        {payError && <p className="mt-3 text-sm text-red-600">{payError}</p>}
      </Wrapper>

      <DemoPaymentModal
        open={demoOrder != null}
        title={`${booking.propertyTitle} · ${formatDate(booking.checkInDate)}`}
        amount={demoOrder?.amount ?? booking.priceBreakdown?.totalAmount ?? 0}
        currency={demoOrder?.currency ?? 'INR'}
        loading={paying}
        onConfirm={() => void handleConfirmDemoPayment()}
        onCancel={() => {
          if (!paying) setDemoOrder(null);
        }}
      />
    </>
  );
}
