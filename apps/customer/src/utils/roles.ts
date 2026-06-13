export const STAFF_ROLES = ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'] as const;

export type StaffRole = (typeof STAFF_ROLES)[number];

export const ADMIN_PORTAL_URL =
  (import.meta.env.VITE_ADMIN_PORTAL_URL as string | undefined) ?? 'http://localhost:5174';

export function isStaffRole(role?: string): role is StaffRole {
  return role != null && (STAFF_ROLES as readonly string[]).includes(role);
}

export function listingApprovalUrl() {
  return `${ADMIN_PORTAL_URL}/listings`;
}
