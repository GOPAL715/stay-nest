import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PageResult } from '../types/api';

interface PropertySummary {
  id: string;
  title: string;
  city: string;
  status: string;
}

interface AvailabilityBlock {
  id: string;
  startDate: string;
  endDate: string;
  reason: string;
}

export function CalendarPage() {
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [message, setMessage] = useState<string | null>(null);

  const { data: listings } = useQuery({
    queryKey: ['my-listings'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/properties/my-listings?page=0&size=50');
      if (!res.ok) throw new Error('Failed to load listings');
      const json = (await res.json()) as ApiEnvelope<PageResult<PropertySummary>>;
      return json.data.content;
    },
  });

  const { data: availability } = useQuery({
    queryKey: ['availability', selectedId],
    enabled: !!selectedId,
    queryFn: async () => {
      const res = await apiFetch(`/api/v1/properties/${selectedId}/availability`);
      if (!res.ok) throw new Error('Failed to load availability');
      const json = (await res.json()) as ApiEnvelope<AvailabilityBlock[]>;
      return json.data;
    },
  });

  const blockDates = async () => {
    if (!selectedId || !startDate || !endDate) return;
    setMessage(null);
    const res = await apiFetch(`/api/v1/properties/${selectedId}/availability/block`, {
      method: 'POST',
      body: JSON.stringify({ startDate, endDate }),
    });
    const json = (await res.json()) as ApiEnvelope<unknown>;
    if (!res.ok || !json.success) {
      setMessage(json.message || 'Failed to block dates');
      return;
    }
    setStartDate('');
    setEndDate('');
    setMessage('Dates blocked successfully');
    void queryClient.invalidateQueries({ queryKey: ['availability', selectedId] });
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Availability Calendar</h2>
      <p className="mt-1 text-sm text-gray-500">Block date ranges on your listings.</p>

      <div className="card mt-6 space-y-4">
        <div>
          <label className="label">Select listing</label>
          <select className="input" value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
            <option value="">Choose a listing…</option>
            {listings?.map((l) => (
              <option key={l.id} value={l.id}>{l.title} ({l.city})</option>
            ))}
          </select>
        </div>

        {selectedId && (
          <>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Block from</label>
                <input type="date" className="input" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
              </div>
              <div>
                <label className="label">Block until</label>
                <input type="date" className="input" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
              </div>
            </div>
            <button type="button" className="btn-primary" onClick={() => void blockDates()}>Block dates</button>
            {message && <p className="text-sm text-gray-600">{message}</p>}

            <div className="mt-4">
              <h3 className="font-medium text-gray-900">Blocked / booked periods</h3>
              <ul className="mt-2 space-y-2">
                {availability?.length === 0 && <li className="text-sm text-gray-500">No blocks yet.</li>}
                {availability?.map((block) => (
                  <li key={block.id} className="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2 text-sm">
                    <span>{block.startDate} → {block.endDate}</span>
                    <span className="badge-gray">{block.reason}</span>
                  </li>
                ))}
              </ul>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
