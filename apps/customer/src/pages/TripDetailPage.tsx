import { Link } from '@tanstack/react-router';
import { useQuery } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { TripBookingCard } from '../components/TripBookingCard';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, Booking } from '../types/api';
import { formatDate } from '../utils/format';

interface TripDetailPageProps {
  bookingId: string;
}

export function TripDetailPage({ bookingId }: TripDetailPageProps) {
  const { data: booking, isLoading, isError } = useQuery({
    queryKey: ['booking', bookingId],
    queryFn: async () => {
      const res = await apiFetch(`/api/v1/bookings/${bookingId}`);
      if (!res.ok) throw new Error('Booking not found');
      const json = (await res.json()) as ApiEnvelope<Booking>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading trip…" />;
  }

  if (isError || !booking) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-16 text-center">
        <p className="text-lg text-gray-900">Trip not found</p>
        <Link to="/trips" className="btn-primary mt-4 inline-flex">
          Back to My Trips
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <Link to="/trips" className="text-sm text-primary-600 hover:text-primary-700">
        ← Back to My Trips
      </Link>

      <h1 className="mt-4 text-2xl font-bold text-gray-900">{booking.propertyTitle}</h1>
      <p className="mt-1 text-gray-500">
        {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}
      </p>

      <div className="mt-6">
        <TripBookingCard booking={booking} detail />
      </div>
    </div>
  );
}
