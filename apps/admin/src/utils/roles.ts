export const STAFF_ROLES = ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'] as const;

export type StaffRole = (typeof STAFF_ROLES)[number];

export function hasRole(userRole: string | undefined, allowed: readonly string[]) {
  return userRole != null && allowed.includes(userRole);
}

export const NAV_ITEMS = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: '📊',
    roles: STAFF_ROLES,
  },
  {
    label: 'Listing Approval',
    path: '/listings',
    icon: '✅',
    roles: ['SUPER_ADMIN', 'PROPERTY_MANAGER'],
  },
  {
    label: 'Applications',
    path: '/applications',
    icon: '📋',
    roles: ['SUPER_ADMIN', 'PROPERTY_MANAGER'],
  },
  {
    label: 'Bookings',
    path: '/bookings',
    icon: '📅',
    roles: ['SUPER_ADMIN', 'SUPPORT_AGENT'],
  },
  {
    label: 'Users',
    path: '/users',
    icon: '👥',
    roles: ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'],
  },
  {
    label: 'Create Admin',
    path: '/users/create-admin',
    icon: '🛡️',
    roles: ['SUPER_ADMIN'],
  },
  {
    label: 'Config',
    path: '/config',
    icon: '⚙️',
    roles: ['SUPER_ADMIN'],
  },
] as const;

export function navItemsForRole(role?: string) {
  if (!role) return [];
  return NAV_ITEMS.filter((item) => hasRole(role, item.roles));
}
