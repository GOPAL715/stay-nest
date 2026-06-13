export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  path: string;
}

export interface FieldValidationError {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  success: false;
  message: string;
  errors?: FieldValidationError[];
  timestamp?: string;
  path?: string;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  profilePictureUrl?: string;
  role: string;
  status: string;
  emailVerified: boolean;
  createdAt: string;
}

export interface AuthData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export interface PropertySummary {
  id: string;
  title: string;
  propertyType: string;
  city: string;
  state: string;
  country: string;
  latitude?: number;
  longitude?: number;
  maxGuests: number;
  bedrooms: number;
  bathrooms: number;
  beds: number;
  basePricePerNight: number;
  cleaningFee: number;
  bookingMode?: string;
  status: string;
  coverPhotoUrl?: string;
  hostFirstName?: string;
  hostLastName?: string;
}

export interface PropertyPhoto {
  id: string;
  url: string;
  caption?: string;
  displayOrder: number;
  cover: boolean;
}

export interface Amenity {
  id: string;
  name: string;
  icon?: string;
  category?: string;
}

export interface PropertyDetail extends PropertySummary {
  description: string;
  addressLine1: string;
  addressLine2?: string;
  postalCode?: string;
  serviceFeePercent?: number;
  cancellationPolicy: string;
  rejectionReason?: string;
  host?: {
    id: string;
    firstName: string;
    lastName: string;
    profilePictureUrl?: string;
    createdAt: string;
  };
  photos: PropertyPhoto[];
  amenities: Amenity[];
  createdAt: string;
  updatedAt: string;
}

export interface PriceBreakdown {
  nightlyRate: number;
  numNights: number;
  subtotal: number;
  cleaningFee: number;
  platformFee: number;
  taxes: number;
  totalAmount: number;
  nightlyRateInr?: string;
  totalAmountInr?: string;
}

export interface Payment {
  id: string;
  bookingId: string;
  razorpayOrderId: string;
  razorpayPaymentId?: string;
  amount: number;
  currency: string;
  status: 'CREATED' | 'PAID' | 'FAILED' | 'REFUNDED';
  createdAt: string;
}

export interface Booking {
  id: string;
  propertyId: string;
  propertyTitle: string;
  propertyCity: string;
  coverPhotoUrl?: string;
  hostId: string;
  hostFirstName: string;
  hostLastName: string;
  guestId: string;
  guestFirstName: string;
  guestLastName: string;
  checkInDate: string;
  checkOutDate: string;
  numGuests: number;
  numNights: number;
  status: string;
  cancellationPolicy: string;
  cancellationReason?: string;
  specialRequests?: string;
  cancelledAt?: string;
  createdAt: string;
  priceBreakdown?: PriceBreakdown;
}
