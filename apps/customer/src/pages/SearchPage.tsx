import { useMemo, useState } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { PropertyCard } from '../components/PropertyCard';
import { PropertyMap } from '../components/PropertyMap';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PageResult, PropertySummary } from '../types/api';

interface SearchFilters {
  city: string;
  checkIn: string;
  checkOut: string;
  numGuests: string;
  minPrice: string;
  maxPrice: string;
  sortBy: string;
  page: number;
}

function buildSearchUrl(filters: SearchFilters): string {
  const params = new URLSearchParams();
  const city = filters.city.trim();
  if (city) params.set('city', city);
  if (filters.checkIn) params.set('checkIn', filters.checkIn);
  if (filters.checkOut) params.set('checkOut', filters.checkOut);
  if (filters.numGuests) params.set('numGuests', filters.numGuests);
  if (filters.minPrice) params.set('minPrice', filters.minPrice);
  if (filters.maxPrice) params.set('maxPrice', filters.maxPrice);
  params.set('sortBy', filters.sortBy);
  params.set('page', String(filters.page));
  params.set('size', '12');
  return `/api/v1/properties?${params.toString()}`;
}

export function SearchPage() {
  const [filters, setFilters] = useState<SearchFilters>({
    city: '',
    checkIn: '',
    checkOut: '',
    numGuests: '2',
    minPrice: '',
    maxPrice: '',
    sortBy: 'newest',
    page: 0,
  });
  const [draft, setDraft] = useState(filters);

  const searchUrl = useMemo(() => buildSearchUrl(filters), [filters]);

  const { data, isLoading, isError, error, isFetching } = useQuery({
    queryKey: ['properties', searchUrl],
    queryFn: async () => {
      const res = await apiFetch(searchUrl);
      if (!res.ok) throw new Error('Failed to load properties');
      const json = (await res.json()) as ApiEnvelope<PageResult<PropertySummary>>;
      return json.data;
    },
    placeholderData: keepPreviousData,
  });

  const applyFilters = (e?: React.FormEvent) => {
    e?.preventDefault();
    setFilters({ ...draft, city: draft.city.trim(), page: 0 });
  };

  const results = data?.content ?? [];

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="text-2xl font-bold text-gray-900">Search Properties</h1>
      <p className="mt-1 text-gray-500">Find your perfect stay by location, dates, and guests.</p>

      <form
        onSubmit={applyFilters}
        className="mt-6 card grid gap-4 rounded-xl border border-gray-200 bg-white p-4 sm:grid-cols-2 lg:grid-cols-6"
      >
        <div className="lg:col-span-2">
          <label className="label">City</label>
          <input
            className="input"
            placeholder="e.g. Bangalore, Goa, Mumbai"
            value={draft.city}
            onChange={(e) => setDraft({ ...draft, city: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Check-in</label>
          <input
            type="date"
            className="input"
            value={draft.checkIn}
            onChange={(e) => setDraft({ ...draft, checkIn: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Check-out</label>
          <input
            type="date"
            className="input"
            value={draft.checkOut}
            onChange={(e) => setDraft({ ...draft, checkOut: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Guests</label>
          <input
            type="number"
            min={1}
            className="input"
            value={draft.numGuests}
            onChange={(e) => setDraft({ ...draft, numGuests: e.target.value })}
          />
        </div>
        <div className="flex items-end">
          <button type="submit" className="btn-primary w-full" disabled={isFetching}>
            {isFetching ? 'Searching…' : 'Search'}
          </button>
        </div>
        <div>
          <label className="label">Min price (paise)</label>
          <input
            className="input"
            placeholder="500000"
            value={draft.minPrice}
            onChange={(e) => setDraft({ ...draft, minPrice: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Max price (paise)</label>
          <input
            className="input"
            placeholder="2000000"
            value={draft.maxPrice}
            onChange={(e) => setDraft({ ...draft, maxPrice: e.target.value })}
          />
        </div>
        <div>
          <label className="label">Sort by</label>
          <select
            className="input"
            value={draft.sortBy}
            onChange={(e) => setDraft({ ...draft, sortBy: e.target.value })}
          >
            <option value="newest">Newest</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
          </select>
        </div>
      </form>

      <div className="mt-8">
        <PropertyMap properties={results} />
      </div>

      {isLoading && !data && <PageLoader message="Loading properties…" />}
      {isError && (
        <p className="mt-6 text-center text-red-600">
          {error instanceof Error ? error.message : 'Something went wrong'}
        </p>
      )}

      {!isLoading && !isError && data && results.length === 0 && (
        <div className="mt-8 text-center">
          <p className="text-lg font-medium text-gray-900">No properties found</p>
          <p className="mt-2 text-gray-500">
            {filters.city
              ? `No active listings matched "${filters.city}". Try another spelling or clear filters.`
              : 'Try searching by city or adjust your filters.'}
          </p>
        </div>
      )}

      {results.length > 0 && (
        <>
          <p className="mt-8 text-sm text-gray-600">
            {data?.totalElements ?? results.length} propert{data?.totalElements === 1 ? 'y' : 'ies'} found
            {filters.city ? ` in "${filters.city}"` : ''}
          </p>
          <div className="mt-4 grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {results.map((property) => (
              <PropertyCard key={property.id} property={property} />
            ))}
          </div>
          <div className="mt-8 flex items-center justify-center gap-4">
            <button
              type="button"
              className="btn-secondary"
              disabled={filters.page === 0}
              onClick={() => setFilters((f) => ({ ...f, page: f.page - 1 }))}
            >
              Previous
            </button>
            <span className="text-sm text-gray-600">
              Page {(data?.number ?? 0) + 1} of {data?.totalPages || 1}
            </span>
            <button
              type="button"
              className="btn-secondary"
              disabled={(data?.number ?? 0) + 1 >= (data?.totalPages ?? 1)}
              onClick={() => setFilters((f) => ({ ...f, page: f.page + 1 }))}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}
