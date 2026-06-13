import { useQuery } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import { RevenueChart } from '../components/RevenueChart';
import type { AdminKpis, ApiEnvelope } from '../types/api';

export function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-kpis'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/admin/analytics/kpis');
      if (!res.ok) throw new Error('Failed to load KPIs');
      const json = (await res.json()) as ApiEnvelope<AdminKpis>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading dashboard…" />;
  }

  const cards = [
    { label: 'Active Listings', value: data?.activeListings ?? '—' },
    { label: 'Bookings This Month', value: data?.bookingsThisMonth ?? '—' },
    { label: 'Pending Moderation', value: data?.pendingModeration ?? '—' },
    { label: 'Revenue (INR)', value: data ? `₹${data.platformRevenueInr}` : '—' },
  ];

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
      <p className="mt-1 text-sm text-gray-500">Platform KPIs and pending actions.</p>
      {isError && <p className="mt-4 text-sm text-red-600">Could not load dashboard data.</p>}
      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map(({ label, value }) => (
          <div key={label} className="card">
            <p className="text-sm font-medium text-gray-500">{label}</p>
            <p className="mt-2 text-3xl font-bold text-gray-900">{value}</p>
          </div>
        ))}
      </div>
      <RevenueChart />
    </div>
  );
}
