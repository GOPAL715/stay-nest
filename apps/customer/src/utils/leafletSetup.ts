import L from 'leaflet';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

// Leaflet's default icon resolves broken CDN paths in bundled apps unless removed.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;

L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const PIN_ICON_SIZE: L.PointExpression = [30, 42];
const PIN_ICON_ANCHOR: L.PointExpression = [15, 42];
const PIN_POPUP_ANCHOR: L.PointExpression = [0, -38];

/** Standard pin for search results and listing location picker */
export const staynestPinIcon = new L.Icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: PIN_ICON_SIZE,
  iconAnchor: PIN_ICON_ANCHOR,
  popupAnchor: PIN_POPUP_ANCHOR,
  shadowSize: [42, 42],
  shadowAnchor: [15, 42],
});

/** Larger branded pin — easier to see on the search map */
export const staynestPropertyPinIcon = L.divIcon({
  className: 'staynest-property-marker',
  html: '<span class="staynest-property-marker__pin" aria-hidden="true"></span>',
  iconSize: [36, 48],
  iconAnchor: [18, 48],
  popupAnchor: [0, -44],
});
