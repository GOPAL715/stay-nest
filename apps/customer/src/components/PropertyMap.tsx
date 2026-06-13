import { useEffect, useMemo } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { staynestPropertyPinIcon } from '../utils/leafletSetup.ts';
import type { PropertySummary } from '../types/api';
import { formatPaise } from '../utils/format';
import { useLeafletMap } from '../hooks/useLeafletMap';

interface PropertyMapProps {
  properties: PropertySummary[];
}

const DEFAULT_CENTER: [number, number] = [20.5937, 78.9629];

function escapeHtml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;');
}

export function PropertyMap({ properties }: PropertyMapProps) {
  const mappable = useMemo(
    () =>
      properties.filter(
        (property) =>
          property.latitude != null &&
          property.longitude != null &&
          !isNaN(Number(property.latitude)) &&
          !isNaN(Number(property.longitude)),
      ),
    [properties],
  );

  const center: [number, number] =
    mappable.length > 0
      ? [Number(mappable[0].latitude), Number(mappable[0].longitude)]
      : DEFAULT_CENTER;

  const { containerRef, mapRef, isReady } = useLeafletMap({
    center,
    zoom: mappable.length > 0 ? 11 : 5,
    scrollWheelZoom: 'hover',
  });

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !isReady) return;

    const markers = mappable.map((property) => {
      const marker = L.marker([Number(property.latitude), Number(property.longitude)], {
        icon: staynestPropertyPinIcon,
      }).addTo(map);

      marker.bindPopup(`
        <div style="font-size:14px;line-height:1.4">
          <p style="font-weight:600;margin:0">${escapeHtml(property.title)}</p>
          <p style="color:#4b5563;margin:4px 0 0">${escapeHtml(property.city)}</p>
          <p style="margin:4px 0 0">${escapeHtml(formatPaise(property.basePricePerNight))}/night</p>
          <a href="/properties/${property.id}" style="display:inline-block;margin-top:8px;color:#dc2626;text-decoration:underline">View listing</a>
        </div>
      `);

      return marker;
    });

    if (markers.length > 0) {
      const group = L.featureGroup(markers);
      map.fitBounds(group.getBounds().pad(0.2));
    } else {
      map.setView(DEFAULT_CENTER, 5);
    }

    return () => {
      markers.forEach((marker) => marker.remove());
    };
  }, [isReady, mappable, mapRef]);

  return (
    <div className="space-y-2">
      <div className="h-96 w-full overflow-hidden rounded-xl border border-gray-200">
        <div ref={containerRef} className="h-full w-full" tabIndex={0} />
      </div>
      <p className="text-xs text-gray-500">
        Hover the map and use the mouse wheel or +/- controls to zoom. Click a pin to view a listing.
      </p>
      {properties.length > 0 && mappable.length === 0 && (
        <p className="text-sm text-amber-700">
          Listings found, but none have map coordinates yet. Open a property card to book.
        </p>
      )}
      {properties.length === 0 && (
        <p className="text-sm text-gray-500">Search by city to see available stays on the map.</p>
      )}
    </div>
  );
}
