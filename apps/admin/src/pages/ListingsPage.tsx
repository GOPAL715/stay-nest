import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PageResult, PropertySummary } from '../types/api';

type ListingTab = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

const TABS: { id: ListingTab; label: string; description: string }[] = [
  { id: 'PENDING', label: 'Pending Review', description: 'Review and approve new submissions.' },
  { id: 'ACTIVE', label: 'Approved', description: 'Live listings visible to guests.' },
  { id: 'SUSPENDED', label: 'Suspended', description: 'Listings removed from guest search.' },
];

function formatPrice(paise: number) {
  return `₹${(paise / 100).toLocaleString('en-IN')}`;
}

function statusBadgeClass(status: string) {
  if (status === 'ACTIVE') return 'badge-green';
  if (status === 'SUSPENDED') return 'badge-red';
  return 'badge-yellow';
}

export function ListingsPage() {
  const queryClient = useQueryClient();
  const [tab, setTab] = useState<ListingTab>('PENDING');
  const [rejectReason, setRejectReason] = useState('');
  const [suspendReason, setSuspendReason] = useState('');
  const [actionId, setActionId] = useState<string | null>(null);
  const [actionType, setActionType] = useState<'reject' | 'suspend' | null>(null);

  const activeTab = TABS.find((t) => t.id === tab)!;

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-listings', tab],
    queryFn: async () => {
      const res = await apiFetch(`/api/v1/admin/listings?status=${tab}&page=0&size=50`);
      if (!res.ok) throw new Error('Failed to load listings');
      const json = (await res.json()) as ApiEnvelope<PageResult<PropertySummary>>;
      return json.data;
    },
  });

  const invalidateListings = () => {
    void queryClient.invalidateQueries({ queryKey: ['admin-listings'] });
  };

  const approve = async (propertyId: string) => {
    await apiFetch(`/api/v1/admin/listings/${propertyId}/approve`, { method: 'PATCH' });
    invalidateListings();
  };

  const reject = async (propertyId: string) => {
    if (!rejectReason.trim()) return;
    await apiFetch(`/api/v1/admin/listings/${propertyId}/reject`, {
      method: 'PATCH',
      body: JSON.stringify({ reason: rejectReason }),
    });
    setActionId(null);
    setActionType(null);
    setRejectReason('');
    invalidateListings();
  };

  const suspend = async (propertyId: string) => {
    if (!suspendReason.trim()) return;
    await apiFetch(`/api/v1/admin/listings/${propertyId}/suspend`, {
      method: 'PATCH',
      body: JSON.stringify({ reason: suspendReason }),
    });
    setActionId(null);
    setActionType(null);
    setSuspendReason('');
    invalidateListings();
  };

  const openAction = (listingId: string, type: 'reject' | 'suspend') => {
    setActionId(listingId);
    setActionType(type);
    setRejectReason('');
    setSuspendReason('');
  };

  const closeAction = () => {
    setActionId(null);
    setActionType(null);
    setRejectReason('');
    setSuspendReason('');
  };

  if (isLoading) {
    return <PageLoader message="Loading listings…" />;
  }

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Listing Approval</h2>
      <p className="mt-1 text-sm text-gray-500">
        Review pending submissions and manage approved listings.
      </p>

      <div className="mt-6 flex flex-wrap gap-2 border-b border-gray-200 pb-3">
        {TABS.map(({ id, label }) => (
          <button
            key={id}
            type="button"
            onClick={() => {
              setTab(id);
              closeAction();
            }}
            className={[
              'rounded-lg px-4 py-2 text-sm font-medium transition-colors',
              tab === id
                ? 'bg-primary-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
            ].join(' ')}
          >
            {label}
            {data && tab === id ? ` (${data.totalElements})` : ''}
          </button>
        ))}
      </div>

      <p className="mt-4 text-sm text-gray-500">{activeTab.description}</p>

      {isError && <p className="mt-6 text-red-600">Failed to load listings.</p>}

      {data && data.content.length === 0 && (
        <p className="mt-8 text-gray-500">
          {tab === 'PENDING' && 'No listings pending review.'}
          {tab === 'ACTIVE' && 'No approved listings yet.'}
          {tab === 'SUSPENDED' && 'No suspended listings.'}
        </p>
      )}

      <ul className="mt-6 space-y-4">
        {data?.content.map((listing) => (
          <li key={listing.id} className="card flex flex-wrap items-center justify-between gap-4">
            <div>
              <h3 className="font-semibold text-gray-900">{listing.title}</h3>
              <p className="text-sm text-gray-500">
                {listing.city}, {listing.state}
                {listing.hostFirstName && (
                  <> · Host: {listing.hostFirstName} {listing.hostLastName}</>
                )}
              </p>
              {listing.basePricePerNight > 0 && (
                <p className="mt-1 text-sm text-gray-700">
                  {formatPrice(listing.basePricePerNight)} / night
                </p>
              )}
              <span className={`${statusBadgeClass(listing.status)} mt-2`}>{listing.status}</span>
            </div>

            {tab === 'PENDING' && (
              <div className="flex flex-wrap gap-2">
                <button type="button" className="btn-primary" onClick={() => void approve(listing.id)}>
                  Approve
                </button>
                <button type="button" className="btn-danger" onClick={() => openAction(listing.id, 'reject')}>
                  Reject
                </button>
              </div>
            )}

            {tab === 'ACTIVE' && (
              <button type="button" className="btn-danger" onClick={() => openAction(listing.id, 'suspend')}>
                Suspend
              </button>
            )}

            {actionId === listing.id && actionType === 'reject' && (
              <div className="w-full mt-2 flex gap-2">
                <input
                  className="input flex-1"
                  placeholder="Rejection reason"
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                />
                <button type="button" className="btn-danger" onClick={() => void reject(listing.id)}>
                  Confirm reject
                </button>
                <button type="button" className="btn-secondary" onClick={closeAction}>
                  Cancel
                </button>
              </div>
            )}

            {actionId === listing.id && actionType === 'suspend' && (
              <div className="w-full mt-2 flex gap-2">
                <input
                  className="input flex-1"
                  placeholder="Suspension reason"
                  value={suspendReason}
                  onChange={(e) => setSuspendReason(e.target.value)}
                />
                <button type="button" className="btn-danger" onClick={() => void suspend(listing.id)}>
                  Confirm suspend
                </button>
                <button type="button" className="btn-secondary" onClick={closeAction}>
                  Cancel
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
