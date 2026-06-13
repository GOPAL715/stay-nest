import type { PropertyDetail } from '../types/api';

export const STEPS = ['Basic Info', 'Location', 'Pricing', 'Review'] as const;

export const PROPERTY_TYPES = ['APARTMENT', 'HOUSE', 'VILLA', 'STUDIO', 'CABIN', 'HOTEL_ROOM'];
export const BOOKING_MODES = ['INSTANT_BOOK', 'REQUEST_TO_BOOK'];
export const CANCELLATION_POLICIES = ['FLEXIBLE', 'MODERATE', 'STRICT'];

export const EDITABLE_STATUSES = ['DRAFT', 'PENDING'] as const;

export interface ListingFormState {
  title: string;
  description: string;
  propertyType: string;
  addressLine1: string;
  city: string;
  state: string;
  country: string;
  maxGuests: number;
  bedrooms: number;
  bathrooms: number;
  beds: number;
  basePricePerNight: number;
  cleaningFee: number;
  bookingMode: string;
  cancellationPolicy: string;
  latitude: number | null;
  longitude: number | null;
}

export const initialListingForm: ListingFormState = {
  title: '',
  description: '',
  propertyType: 'APARTMENT',
  addressLine1: '',
  city: '',
  state: '',
  country: 'India',
  maxGuests: 2,
  bedrooms: 1,
  bathrooms: 1,
  beds: 1,
  basePricePerNight: 500000,
  cleaningFee: 50000,
  bookingMode: 'REQUEST_TO_BOOK',
  cancellationPolicy: 'MODERATE',
  latitude: null,
  longitude: null,
};

export function listingFormFromProperty(property: PropertyDetail): ListingFormState {
  return {
    title: property.title,
    description: property.description,
    propertyType: property.propertyType,
    addressLine1: property.addressLine1,
    city: property.city,
    state: property.state,
    country: property.country,
    maxGuests: property.maxGuests,
    bedrooms: property.bedrooms,
    bathrooms: property.bathrooms,
    beds: property.beds,
    basePricePerNight: property.basePricePerNight,
    cleaningFee: property.cleaningFee,
    bookingMode: property.bookingMode ?? 'REQUEST_TO_BOOK',
    cancellationPolicy: property.cancellationPolicy,
    latitude: property.latitude ?? null,
    longitude: property.longitude ?? null,
  };
}

export function buildListingPayload(form: ListingFormState) {
  return {
    ...form,
    bathrooms: form.bathrooms,
    latitude: form.latitude ?? undefined,
    longitude: form.longitude ?? undefined,
  };
}
