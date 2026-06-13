import { useQuery } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';

interface MonthlyRevenue {
  monthLabel: string;
  platformFeeInr: string;
  bookingCount: number;
}

export function RevenueChart() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['monthly-revenue'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/admin/analytics/revenue/monthly?months=6');
      if (!res.ok) throw new Error('Failed to load revenue');
      const json = (await res.json()) as ApiEnvelope<MonthlyRevenue[]>;
      return json.data.reverse();
    },
  });

  if (isLoading) return <PageLoader message="Loading revenue chart…" />;
  if (isError || !data?.length) return <p className="text-sm text-gray-500">No revenue data available.</p>;

  const maxFee = Math.max(...data.map((d) => parseFloat(d.platformFeeInr) || 0), 1);

  return (
    <div className="card mt-8">
      <h3 className="text-lg font-semibold text-gray-900">Monthly Platform Revenue</h3>
      <div className="mt-6 flex items-end gap-3 h-48">
        {data.map((point) => {
          const fee = parseFloat(point.platformFeeInr) || 0;
          const heightPct = Math.max(4, (fee / maxFee) * 100);
          return (
            <div key={point.monthLabel} className="flex flex-1 flex-col items-center gap-2">
              <span className="text-xs text-gray-500">₹{point.platformFeeInr}</span>
              <div
                className="w-full rounded-t-md bg-primary-500 transition-all"
                style={{ height: `${heightPct}%` }}
                title={`${point.bookingCount} bookings`}
              />
              <span className="text-xs text-gray-600 text-center">{point.monthLabel}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
