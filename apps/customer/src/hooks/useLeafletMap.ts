import { useEffect, useRef, useState } from 'react';
import L, { type Map as LeafletMap } from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../utils/leafletSetup.ts';

export const OSM_TILE_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
export const OSM_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

export interface UseLeafletMapOptions {
  center: [number, number];
  zoom: number;
  /** true = always zoom with wheel; 'hover' = zoom only while pointer is over map (default) */
  scrollWheelZoom?: boolean | 'hover';
}

function cleanupContainer(container: HTMLDivElement) {
  delete (container as HTMLDivElement & { _leaflet_id?: number })._leaflet_id;
}

function configureScrollWheelZoom(
  map: LeafletMap,
  container: HTMLDivElement,
  mode: boolean | 'hover',
): () => void {
  if (mode === true) {
    map.scrollWheelZoom.enable();
    return () => undefined;
  }

  map.scrollWheelZoom.disable();
  const enable = () => map.scrollWheelZoom.enable();
  const disable = () => map.scrollWheelZoom.disable();

  container.addEventListener('mouseenter', enable);
  container.addEventListener('mouseleave', disable);
  container.addEventListener('focusin', enable);
  container.addEventListener('focusout', disable);

  return () => {
    container.removeEventListener('mouseenter', enable);
    container.removeEventListener('mouseleave', disable);
    container.removeEventListener('focusin', enable);
    container.removeEventListener('focusout', disable);
  };
}

export function useLeafletMap({
  center,
  zoom,
  scrollWheelZoom = 'hover',
}: UseLeafletMapOptions) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<LeafletMap | null>(null);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    cleanupContainer(container);

    const map = L.map(container, {
      scrollWheelZoom: false,
      zoomControl: true,
    });
    map.setView(center, zoom);
    L.tileLayer(OSM_TILE_URL, { attribution: OSM_ATTRIBUTION }).addTo(map);

    const teardownWheel = configureScrollWheelZoom(map, container, scrollWheelZoom);

    mapRef.current = map;
    setIsReady(true);

    return () => {
      teardownWheel();
      setIsReady(false);
      mapRef.current = null;
      map.remove();
      cleanupContainer(container);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps -- map is created once per mount
  }, [scrollWheelZoom]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    map.setView(center, zoom);
  }, [center, zoom]);

  return { containerRef, mapRef, isReady };
}
