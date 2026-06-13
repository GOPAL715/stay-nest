import { useNavigate } from '@tanstack/react-router';
import { useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';
import { ListingFormWizard } from './ListingFormWizard';
import { buildListingPayload, initialListingForm, type ListingFormState } from './listingFormShared';

export function ListingCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const handleSubmit = async (form: ListingFormState) => {
    const res = await apiFetch('/api/v1/properties', {
      method: 'POST',
      body: JSON.stringify(buildListingPayload(form)),
    });
    const json = (await res.json()) as ApiEnvelope<{ id: string }>;
    if (!res.ok || !json.success) throw new Error(json.message || 'Failed to create listing');
    await queryClient.invalidateQueries({ queryKey: ['my-listings'] });
    await queryClient.invalidateQueries({ queryKey: ['my-listings-for-calendar'] });
    void navigate({ to: '/listings' });
  };

  return <ListingFormWizard mode="create" initialForm={initialListingForm} onSubmit={handleSubmit} />;
}
