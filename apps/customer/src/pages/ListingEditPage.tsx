import { Link, useNavigate } from '@tanstack/react-router';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PropertyDetail } from '../types/api';
import { ListingFormWizard } from './ListingFormWizard';
import {
  buildListingPayload,
  EDITABLE_STATUSES,
  listingFormFromProperty,
} from './listingFormShared';

interface ListingEditPageProps {
  listingId: string;
}

export function ListingEditPage({ listingId }: ListingEditPageProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: property, isLoading, isError } = useQuery({
    queryKey: ['property', listingId, 'edit'],
    queryFn: async () => {
      const res = await apiFetch(`/api/v1/properties/${listingId}`);
      if (!res.ok) throw new Error('Listing not found');
      const json = (await res.json()) as ApiEnvelope<PropertyDetail>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading listing…" />;
  }

  if (isError || !property) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-8 text-center">
        <p className="text-red-600">Could not load this listing.</p>
        <Link to="/listings" className="btn-primary mt-4 inline-flex">
          Back to My Listings
        </Link>
      </div>
    );
  }

  if (!EDITABLE_STATUSES.includes(property.status as (typeof EDITABLE_STATUSES)[number])) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-8 text-center">
        <h2 className="text-2xl font-bold text-gray-900">Cannot edit this listing</h2>
        <p className="mt-2 text-sm text-gray-600">
          Active or suspended listings cannot be edited. Contact support if you need changes.
        </p>
        <Link to="/listings" className="btn-primary mt-6 inline-flex">
          Back to My Listings
        </Link>
      </div>
    );
  }

  const handleSubmit = async (form: ReturnType<typeof listingFormFromProperty>) => {
    const res = await apiFetch(`/api/v1/properties/${listingId}`, {
      method: 'PUT',
      body: JSON.stringify(buildListingPayload(form)),
    });
    const json = (await res.json()) as ApiEnvelope<{ id: string }>;
    if (!res.ok || !json.success) throw new Error(json.message || 'Failed to update listing');
    await queryClient.invalidateQueries({ queryKey: ['my-listings'] });
    await queryClient.invalidateQueries({ queryKey: ['my-listings-for-calendar'] });
    await queryClient.invalidateQueries({ queryKey: ['property', listingId] });
    void navigate({ to: '/listings' });
  };

  return (
    <ListingFormWizard
      mode="edit"
      initialForm={listingFormFromProperty(property)}
      onSubmit={handleSubmit}
    />
  );
}
