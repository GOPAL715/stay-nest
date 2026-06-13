import { useQuery } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, Booking, PageResult } from '../types/api';

export function BookingsPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-bookings'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/admin/bookings?page=0&size=20');
      if (!res.ok) throw new Error('Failed to load bookings');
      const json = (await res.json()) as ApiEnvelope<PageResult<Booking>>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading bookings…" />;
  }

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Bookings</h2>
      <p className="mt-1 text-sm text-gray-500">All platform bookings.</p>
      {isError && <p className="mt-6 text-red-600">Failed to load bookings.</p>}
      <div className="mt-6 overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 rounded-xl border border-gray-200 bg-white">
          <thead className="bg-gray-50">
            <tr>
              {['Property', 'Guest', 'Host', 'Dates', 'Status', 'Total'].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {data?.content.map((b) => (
              <tr key={b.id}>
                <td className="px-4 py-3 text-sm">{b.propertyTitle}</td>
                <td className="px-4 py-3 text-sm">{b.guestFirstName} {b.guestLastName}</td>
                <td className="px-4 py-3 text-sm">{b.hostFirstName} {b.hostLastName}</td>
                <td className="px-4 py-3 text-sm">{b.checkInDate} → {b.checkOutDate}</td>
                <td className="px-4 py-3"><span className="badge-gray">{b.status}</span></td>
                <td className="px-4 py-3 text-sm">
                  {b.priceBreakdown?.totalAmountInr ? `₹${b.priceBreakdown.totalAmountInr}` : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
