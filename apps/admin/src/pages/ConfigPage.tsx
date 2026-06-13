import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PlatformConfigEntry } from '../types/api';

export function ConfigPage() {
  const queryClient = useQueryClient();
  const [editingKey, setEditingKey] = useState<string | null>(null);
  const [editValue, setEditValue] = useState('');

  const { data, isLoading, isError } = useQuery({
    queryKey: ['platform-config'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/admin/config');
      if (!res.ok) throw new Error('Failed to load config');
      const json = (await res.json()) as ApiEnvelope<PlatformConfigEntry[]>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading configuration…" />;
  }

  const save = async (configKey: string) => {
    await apiFetch(`/api/v1/admin/config/${configKey}`, {
      method: 'PUT',
      body: JSON.stringify({ configValue: editValue }),
    });
    setEditingKey(null);
    void queryClient.invalidateQueries({ queryKey: ['platform-config'] });
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Platform Configuration</h2>
      <p className="mt-1 text-sm text-gray-500">Manage platform-wide settings (Super Admin only).</p>
      {isError && <p className="mt-6 text-red-600">Failed to load configuration.</p>}
      <ul className="mt-6 space-y-4">
        {data?.map((entry) => (
          <li key={entry.id} className="card">
            <p className="font-mono text-sm font-semibold text-gray-900">{entry.configKey}</p>
            {entry.description && <p className="mt-1 text-sm text-gray-500">{entry.description}</p>}
            {editingKey === entry.configKey ? (
              <div className="mt-3 flex gap-2">
                <input className="input flex-1" value={editValue} onChange={(e) => setEditValue(e.target.value)} />
                <button type="button" className="btn-primary" onClick={() => void save(entry.configKey)}>Save</button>
                <button type="button" className="btn-secondary" onClick={() => setEditingKey(null)}>Cancel</button>
              </div>
            ) : (
              <div className="mt-3 flex items-center justify-between">
                <span className="text-lg font-medium text-gray-900">{entry.configValue}</span>
                <button
                  type="button"
                  className="btn-secondary text-sm"
                  onClick={() => { setEditingKey(entry.configKey); setEditValue(entry.configValue); }}
                >
                  Edit
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
