import { Link } from '@tanstack/react-router';
import type { PropertySummary } from '../types/api';
import { formatPaise } from '../utils/format';

interface PropertyCardProps {
  property: PropertySummary;
}

export function PropertyCard({ property }: PropertyCardProps) {
  const photoUrl =
    property.coverPhotoUrl ||
    'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800&q=80';

  return (
    <Link
      to="/properties/$propertyId"
      params={{ propertyId: property.id }}
      className="group block overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:shadow-md"
    >
      <div className="aspect-[4/3] overflow-hidden bg-gray-100">
        <img
          src={photoUrl}
          alt={property.title}
          className="h-full w-full object-cover transition group-hover:scale-105"
        />
      </div>
      <div className="p-4">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-gray-900 line-clamp-1">{property.title}</h3>
          <span className="shrink-0 text-xs rounded-full bg-gray-100 px-2 py-0.5 text-gray-600">
            {property.propertyType.replace('_', ' ')}
          </span>
        </div>
        <p className="mt-1 text-sm text-gray-500">
          {property.city}, {property.state}
        </p>
        <p className="mt-2 text-sm text-gray-600">
          {property.maxGuests} guests · {property.bedrooms} bed · {property.beds} beds
        </p>
        <p className="mt-3 text-base font-semibold text-gray-900">
          {formatPaise(property.basePricePerNight)}{' '}
          <span className="text-sm font-normal text-gray-500">/ night</span>
        </p>
      </div>
    </Link>
  );
}
