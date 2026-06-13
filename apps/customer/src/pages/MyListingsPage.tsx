import { useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PageResult, PropertySummary } from '../types/api';
import { Link } from '@tanstack/react-router';
import { useState } from 'react';
import { PageLoader } from '../components/PageLoader';
import { EDITABLE_STATUSES } from './listingFormShared';

export function MyListingsPage() {
  const queryClient = useQueryClient();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['my-listings'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/properties/my-listings?page=0&size=50');
      if (!res.ok) throw new Error('Failed to load your listings');
      const json = (await res.json()) as ApiEnvelope<PageResult<PropertySummary>>;
      return json.data;
    },
    staleTime: 0,
  });

  if (isLoading && !data) {
    return <PageLoader message="Loading your listings…" />;
  }

  const submitForReview = async (propertyId: string) => {
    setSubmitError(null);
    try {
      const res = await apiFetch(`/api/v1/properties/${propertyId}/submit`, {
        method: 'PATCH',
      });
      const json = (await res.json()) as ApiEnvelope<any>;
      if (!res.ok || !json.success) throw new Error(json.message || 'Failed to submit for review');
      void queryClient.invalidateQueries({ queryKey: ['my-listings'] });
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Submission failed');
    }
  };

  const hasListings = data?.content && data.content.length > 0;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-200 pb-5">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-gray-900">My Listings</h2>
          <p className="mt-2 text-sm text-gray-500">Manage your properties, pricing, and submission status.</p>
        </div>
        <Link to="/listings/new" className="btn-primary">
          Create Listing
        </Link>
      </div>

      {submitError && (
        <div className="mt-4 rounded-md bg-red-50 p-4 text-sm text-red-600">
          {submitError}
        </div>
      )}

      {isError && <p className="mt-8 text-center text-red-600">Failed to load listings.</p>}

      {!isError && !hasListings && (
        <div className="mt-12 rounded-lg border-2 border-dashed border-gray-300 p-12 text-center text-gray-500">
          <span className="text-4xl">🏠</span>
          <h3 className="mt-2 text-lg font-semibold text-gray-900">No properties listed yet</h3>
          <p className="mt-1 text-sm text-gray-500">Get started by creating your first listing on StayNest.</p>
          <Link to="/listings/new" className="btn-primary mt-6 inline-flex">
            Create Listing
          </Link>
        </div>
      )}

      {hasListings && (
        <div className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {data?.content.map((listing) => (
            <div key={listing.id} className="card flex flex-col justify-between">
              <div>
                <h3 className="text-lg font-bold text-gray-900 truncate">{listing.title}</h3>
                <p className="text-sm text-gray-500">
                  {listing.city}, {listing.state}, {listing.country}
                </p>
                <div className="mt-4 flex gap-4 text-xs text-gray-500">
                  <span>👤 {listing.maxGuests} guests</span>
                  <span>🛏️ {listing.bedrooms} beds</span>
                  <span>🛁 {listing.bathrooms} baths</span>
                </div>
                <div className="mt-4 flex items-center justify-between">
                  <span className="text-sm font-semibold text-gray-900">
                    ₹{(listing.basePricePerNight / 100).toLocaleString('en-IN')} / night
                  </span>
                  <span
                    className={[
                      'rounded-full px-2.5 py-0.5 text-xs font-semibold',
                      listing.status === 'ACTIVE'
                        ? 'bg-green-100 text-green-800'
                        : listing.status === 'PENDING'
                        ? 'bg-yellow-100 text-yellow-800'
                        : 'bg-gray-100 text-gray-800',
                    ].join(' ')}
                  >
                    {listing.status}
                  </span>
                </div>
              </div>

              <div className="mt-6 flex flex-wrap gap-2 border-t border-gray-150 pt-4">
                <Link to={`/properties/${listing.id}`} className="btn-secondary flex-1 text-center text-xs">
                  View Detail
                </Link>
                {EDITABLE_STATUSES.includes(listing.status as (typeof EDITABLE_STATUSES)[number]) && (
                  <Link
                    to="/listings/$listingId/edit"
                    params={{ listingId: listing.id }}
                    className="btn-secondary flex-1 text-center text-xs"
                  >
                    Edit
                  </Link>
                )}
                {listing.status === 'DRAFT' && (
                  <button
                    type="button"
                    onClick={() => void submitForReview(listing.id)}
                    className="btn-primary flex-1 text-xs"
                  >
                    Submit Review
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
