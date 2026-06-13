import 'leaflet/dist/leaflet.css';
import { staynestPinIcon } from '../utils/leafletSetup.ts';

import L from 'leaflet';
import { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope } from '../types/api';
import { useLeafletMap } from '../hooks/useLeafletMap';

const DEFAULT_CENTER: [number, number] = [15.2993, 74.124];

export interface LocationValue {
  latitude: number;
  longitude: number;
}

interface LocationPickerProps {
  addressLine1: string;
  city: string;
  state: string;
  country: string;
  value: LocationValue | null;
  onChange: (location: LocationValue | null) => void;
}

interface GeocodeResponse {
  latitude: number;
  longitude: number;
  displayName?: string;
  approximate?: boolean;
}

function buildGeocodeUrl(addressLine1: string, city: string, state: string, country: string) {
  const params = new URLSearchParams({
    addressLine1,
    city,
    state,
    country,
  });
  return `/api/v1/geocode?${params.toString()}`;
}

function hasRequiredAddress(addressLine1: string, city: string, state: string, country: string) {
  return [addressLine1, city, state, country].every((part) => part.trim().length > 0);
}

export function LocationPicker({
  addressLine1,
  city,
  state,
  country,
  value,
  onChange,
}: LocationPickerProps) {
  const [geocoding, setGeocoding] = useState(false);
  const [geocodeError, setGeocodeError] = useState<string | null>(null);
  const [displayName, setDisplayName] = useState<string | null>(null);
  const [approximateMatch, setApproximateMatch] = useState(false);
  const onChangeRef = useRef(onChange);
  onChangeRef.current = onChange;

  const center: [number, number] = value
    ? [value.latitude, value.longitude]
    : DEFAULT_CENTER;

  const { containerRef, mapRef, isReady } = useLeafletMap({
    center,
    zoom: 14,
    scrollWheelZoom: 'hover',
  });

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !isReady || !value) return;

    const marker = L.marker([value.latitude, value.longitude], {
      draggable: true,
      icon: staynestPinIcon,
    }).addTo(map);

    marker.on('dragend', () => {
      const { lat, lng } = marker.getLatLng();
      onChangeRef.current({ latitude: lat, longitude: lng });
    });

    return () => {
      marker.remove();
    };
  }, [isReady, mapRef, value?.latitude, value?.longitude]);

  const lookupAddress = async () => {
    if (!hasRequiredAddress(addressLine1, city, state, country)) {
      setGeocodeError('Fill in address, city, state, and country first.');
      return;
    }

    setGeocoding(true);
    setGeocodeError(null);
    try {
      const res = await apiFetch(buildGeocodeUrl(addressLine1, city, state, country));
      const json = (await res.json()) as ApiEnvelope<GeocodeResponse>;
      if (!res.ok || !json.success || !json.data) {
        throw new Error(json.message || 'Could not find that address on the map.');
      }

      onChange({
        latitude: Number(json.data.latitude),
        longitude: Number(json.data.longitude),
      });
      setDisplayName(json.data.displayName ?? null);
      setApproximateMatch(Boolean(json.data.approximate));
    } catch (err) {
      setGeocodeError(err instanceof Error ? err.message : 'Geocoding failed.');
    } finally {
      setGeocoding(false);
    }
  };

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <p className="text-sm font-medium text-gray-900">Pin location on map</p>
          <p className="text-xs text-gray-500">
            We use free OpenStreetMap geocoding. Drag the pin if the auto location is slightly off.
          </p>
        </div>
        <button
          type="button"
          className="btn-secondary text-sm"
          disabled={geocoding}
          onClick={() => void lookupAddress()}
        >
          {geocoding ? 'Finding…' : 'Find on map'}
        </button>
      </div>

      {geocodeError && <p className="text-sm text-red-600">{geocodeError}</p>}
      {approximateMatch && !geocodeError && (
        <p className="text-sm text-amber-700">
          Exact street not found — placed at city center. Drag the pin to your property location.
        </p>
      )}
      {displayName && <p className="text-xs text-gray-500">Matched: {displayName}</p>}

      <div className="h-72 w-full overflow-hidden rounded-xl border border-gray-200">
        <div ref={containerRef} className="h-full w-full" tabIndex={0} />
      </div>
      <p className="text-xs text-gray-500">Hover the map and scroll to zoom. Drag the pin to fine-tune location.</p>

      {value && (
        <p className="text-xs text-gray-500">
          Coordinates: {value.latitude.toFixed(6)}, {value.longitude.toFixed(6)}
        </p>
      )}
    </div>
  );
}
