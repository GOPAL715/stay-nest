import { useQuery } from '@tanstack/react-query';
import { Link } from '@tanstack/react-router';
import { PageLoader } from '../components/PageLoader';
import { TripBookingCard } from '../components/TripBookingCard';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, Booking, PageResult } from '../types/api';

export function MyTripsPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['my-trips'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/bookings/my-trips?page=0&size=20');
      if (!res.ok) throw new Error('Failed to load trips');
      const json = (await res.json()) as ApiEnvelope<PageResult<Booking>>;
      return json.data;
    },
    staleTime: 0,
  });

  if (isLoading && !data) {
    return <PageLoader message="Loading trips…" />;
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      <h1 className="text-2xl font-bold text-gray-900">My Trips</h1>
      <p className="mt-1 text-gray-500">Your upcoming and past bookings.</p>

      {isError && <p className="mt-10 text-center text-red-600">Could not load your trips.</p>}

      {data && data.content.length === 0 && (
        <div className="mt-16 text-center">
          <p className="text-lg font-medium text-gray-900">No trips yet</p>
          <p className="mt-2 text-gray-500">Start exploring and book your first stay.</p>
          <Link to="/search" className="btn-primary mt-6 inline-flex">
            Search properties
          </Link>
        </div>
      )}

      {data && data.content.length > 0 && (
        <ul className="mt-8 space-y-4">
          {data.content.map((booking) => (
            <TripBookingCard key={booking.id} booking={booking} />
          ))}
        </ul>
      )}
    </div>
  );
}
