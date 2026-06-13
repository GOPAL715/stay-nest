import { useState } from 'react';
import { Link, useNavigate } from '@tanstack/react-router';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, Booking, PropertyDetail } from '../types/api';
import {
  fieldErrorsFromApi,
  formatApiErrorMessage,
  minCheckInDate,
  minCheckOutDate,
  readApiErrorResponse,
} from '../utils/apiError';
import { formatPaise } from '../utils/format';
import { useAuth } from '../hooks/useAuth';
import { isStaffRole, listingApprovalUrl } from '../utils/roles';

interface PropertyDetailPageProps {
  propertyId: string;
}

export function PropertyDetailPage({ propertyId }: PropertyDetailPageProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const isHost = user?.role === 'HOST';
  const isStaff = isStaffRole(user?.role);
  const canApproveListings = user?.role === 'PROPERTY_MANAGER' || user?.role === 'SUPER_ADMIN';

  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [numGuests, setNumGuests] = useState(2);
  const [specialRequests, setSpecialRequests] = useState('');
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [bookingSuccess, setBookingSuccess] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [bookingLoading, setBookingLoading] = useState(false);

  const { data: property, isLoading, isError } = useQuery({
    queryKey: ['property', propertyId],
    queryFn: async () => {
      const res = await apiFetch(`/api/v1/properties/${propertyId}`);
      if (!res.ok) throw new Error('Property not found');
      const json = (await res.json()) as ApiEnvelope<PropertyDetail>;
      return json.data;
    },
  });

  const handleReserve = async () => {
    const nextFieldErrors: Record<string, string> = {};

    if (!checkIn) {
      nextFieldErrors.checkInDate = 'Check-in date is required';
    } else if (checkIn < minCheckInDate()) {
      nextFieldErrors.checkInDate = 'Check-in date must be in the future';
    }

    if (!checkOut) {
      nextFieldErrors.checkOutDate = 'Check-out date is required';
    } else if (checkIn && checkOut <= checkIn) {
      nextFieldErrors.checkOutDate = 'Check-out date must be after check-in date';
    }

    if (numGuests < 1) {
      nextFieldErrors.numGuests = 'Must have at least 1 guest';
    } else if (property && numGuests > property.maxGuests) {
      nextFieldErrors.numGuests = `This property accommodates maximum ${property.maxGuests} guests`;
    }

    if (Object.keys(nextFieldErrors).length > 0) {
      setFieldErrors(nextFieldErrors);
      setBookingError('Please fix the highlighted fields');
      return;
    }

    setBookingLoading(true);
    setBookingError(null);
    setBookingSuccess(null);
    setFieldErrors({});
    try {
      const res = await apiFetch('/api/v1/bookings', {
        method: 'POST',
        body: JSON.stringify({
          propertyId,
          checkInDate: checkIn,
          checkOutDate: checkOut,
          numGuests,
          specialRequests: specialRequests || undefined,
        }),
      });
      const json = await readApiErrorResponse(res);
      if (!res.ok || !json.success) {
        const apiFieldErrors = fieldErrorsFromApi(json.errors);
        setFieldErrors(apiFieldErrors);
        setBookingError(formatApiErrorMessage(json));
        return;
      }
      const booking = (json as unknown as ApiEnvelope<Booking>).data;
      void queryClient.invalidateQueries({ queryKey: ['my-trips'] });

      if (booking.status === 'CONFIRMED') {
        setBookingSuccess('Booking confirmed! Complete payment from My Trips.');
      } else {
        setBookingSuccess('Request sent! Track status in My Trips once the host approves.');
      }

      void navigate({ to: '/trips' });
    } catch {
      setBookingError('Network error. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (isLoading) {
    return <PageLoader message="Loading property…" />;
  }

  if (isError || !property) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-16 text-center">
        <p className="text-lg text-gray-900">Property not found</p>
        <Link to="/search" className="btn-primary mt-4 inline-flex">Back to search</Link>
      </div>
    );
  }

  const coverPhoto =
    property.photos?.find((p) => p.cover)?.url ||
    property.photos?.[0]?.url ||
    property.coverPhotoUrl ||
    'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1200&q=80';

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      <Link to="/search" className="text-sm text-primary-600 hover:text-primary-700">
        ← Back to search
      </Link>

      <div className="mt-4 overflow-hidden rounded-2xl">
        <img src={coverPhoto} alt={property.title} className="h-80 w-full object-cover" />
      </div>

      <div className="mt-8 grid gap-8 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <h1 className="text-3xl font-bold text-gray-900">{property.title}</h1>
          <p className="mt-2 text-gray-500">
            {property.city}, {property.state}, {property.country}
          </p>
          <p className="mt-4 text-gray-700">{property.description}</p>

          <div className="mt-6 flex flex-wrap gap-4 text-sm text-gray-600">
            <span>{property.maxGuests} guests</span>
            <span>{property.bedrooms} bedrooms</span>
            <span>{property.beds} beds</span>
            <span>{property.bookingMode?.replace('_', ' ') ?? 'Request to book'}</span>
          </div>

          {property.amenities?.length > 0 && (
            <div className="mt-8">
              <h2 className="text-lg font-semibold text-gray-900">Amenities</h2>
              <ul className="mt-3 flex flex-wrap gap-2">
                {property.amenities.map((a) => (
                  <li key={a.id} className="rounded-full bg-gray-100 px-3 py-1 text-sm text-gray-700">
                    {a.name}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {property.host && (
            <div className="mt-8 rounded-xl border border-gray-200 p-4">
              <h2 className="font-semibold text-gray-900">Hosted by</h2>
              <p className="mt-1 text-gray-600">
                {property.host.firstName} {property.host.lastName}
              </p>
            </div>
          )}
        </div>

        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm h-fit">
          <p className="text-2xl font-bold text-gray-900">
            {formatPaise(property.basePricePerNight)}{' '}
            <span className="text-base font-normal text-gray-500">/ night</span>
          </p>
          <p className="mt-1 text-sm text-gray-500">
            Cleaning fee: {formatPaise(property.cleaningFee)}
          </p>

          {isHost ? (
            <div className="mt-6 space-y-3">
              <p className="text-sm text-gray-600">
                Host accounts manage listings and guest requests — they cannot book stays here.
              </p>
              <Link to="/listings" className="btn-primary w-full text-center">
                Go to My Listings
              </Link>
              <Link to="/booking-requests" className="btn-secondary w-full text-center">
                Guest booking requests
              </Link>
            </div>
          ) : isStaff ? (
            <div className="mt-6 space-y-3">
              <p className="text-sm text-gray-600">
                Staff accounts cannot book stays on the customer site.
                {canApproveListings
                  ? ' Approve listings in the admin portal.'
                  : ' Use the admin portal for platform tasks.'}
              </p>
              <Link to="/staff-portal" className="btn-primary w-full text-center">
                Go to Admin Portal
              </Link>
              {canApproveListings && (
                <a href={listingApprovalUrl()} className="btn-secondary w-full text-center">
                  Open Listing Approval
                </a>
              )}
            </div>
          ) : (
            <>
              <div className="mt-6 space-y-3">
                <div>
                  <label className="label">Check-in</label>
                  <input
                    type="date"
                    className={`input ${fieldErrors.checkInDate ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''}`}
                    value={checkIn}
                    min={minCheckInDate()}
                    onChange={(e) => {
                      setCheckIn(e.target.value);
                      setFieldErrors((prev) => {
                        const next = { ...prev };
                        delete next.checkInDate;
                        return next;
                      });
                      setBookingError(null);
                    }}
                  />
                  {fieldErrors.checkInDate && (
                    <p className="mt-1 text-sm text-red-600">{fieldErrors.checkInDate}</p>
                  )}
                </div>
                <div>
                  <label className="label">Check-out</label>
                  <input
                    type="date"
                    className={`input ${fieldErrors.checkOutDate ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''}`}
                    value={checkOut}
                    min={minCheckOutDate(checkIn)}
                    onChange={(e) => {
                      setCheckOut(e.target.value);
                      setFieldErrors((prev) => {
                        const next = { ...prev };
                        delete next.checkOutDate;
                        return next;
                      });
                      setBookingError(null);
                    }}
                  />
                  {fieldErrors.checkOutDate && (
                    <p className="mt-1 text-sm text-red-600">{fieldErrors.checkOutDate}</p>
                  )}
                </div>
                <div>
                  <label className="label">Guests</label>
                  <input
                    type="number"
                    min={1}
                    max={property.maxGuests}
                    className={`input ${fieldErrors.numGuests ? 'border-red-500 focus:border-red-500 focus:ring-red-500' : ''}`}
                    value={numGuests}
                    onChange={(e) => {
                      setNumGuests(Number(e.target.value));
                      setFieldErrors((prev) => {
                        const next = { ...prev };
                        delete next.numGuests;
                        return next;
                      });
                      setBookingError(null);
                    }}
                  />
                  {fieldErrors.numGuests && (
                    <p className="mt-1 text-sm text-red-600">{fieldErrors.numGuests}</p>
                  )}
                </div>
                <div>
                  <label className="label">Special requests</label>
                  <textarea
                    className="input min-h-[80px]"
                    value={specialRequests}
                    onChange={(e) => setSpecialRequests(e.target.value)}
                  />
                </div>
              </div>

              {bookingSuccess && (
                <div className="mt-3 rounded-md bg-green-50 p-3 text-sm text-green-700" role="status">
                  {bookingSuccess}
                </div>
              )}

              {bookingError && (
                <div className="mt-3 rounded-md bg-red-50 p-3 text-sm text-red-600" role="alert">
                  {bookingError}
                </div>
              )}

              <button
                type="button"
                onClick={handleReserve}
                disabled={bookingLoading}
                className="btn-primary mt-6 w-full"
              >
                {bookingLoading ? 'Reserving…' : property.bookingMode === 'INSTANT_BOOK' ? 'Reserve' : 'Request to book'}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
