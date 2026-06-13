export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  status: string;
  emailVerified: boolean;
}

export interface AuthData {
  accessToken: string;
  refreshToken: string;
  user: UserProfile;
}

export interface PropertySummary {
  id: string;
  title: string;
  city: string;
  state: string;
  status: string;
  basePricePerNight: number;
  hostFirstName?: string;
  hostLastName?: string;
}

export interface Booking {
  id: string;
  propertyTitle: string;
  propertyCity: string;
  guestFirstName: string;
  guestLastName: string;
  hostFirstName: string;
  hostLastName: string;
  checkInDate: string;
  checkOutDate: string;
  status: string;
  priceBreakdown?: { totalAmountInr?: string; totalAmount: number };
}

export interface HostApplication {
  id: string;
  applicant: UserProfile;
  status: string;
  motivation: string;
  reviewNotes?: string;
  createdAt: string;
}

export interface PlatformConfigEntry {
  id: string;
  configKey: string;
  configValue: string;
  description?: string;
}

export interface AdminKpis {
  activeListings: number;
  bookingsThisMonth: number;
  pendingModeration: number;
  platformRevenuePaise: number;
  platformRevenueInr: string;
}
