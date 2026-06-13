import { Link } from '@tanstack/react-router';
import { RoleBadge } from '../components/RoleBadge';
import { useAuth } from '../hooks/useAuth';
import { ADMIN_PORTAL_URL, isStaffRole, listingApprovalUrl } from '../utils/roles';

export function StaffPortalPage() {
  const { user, logout } = useAuth();

  if (!user || !isStaffRole(user.role)) {
    return <Link to="/search" className="btn-primary">Go to search</Link>;
  }

  const showListingApproval = user.role === 'PROPERTY_MANAGER' || user.role === 'SUPER_ADMIN';

  return (
    <div className="mx-auto max-w-lg px-4 py-16 text-center sm:px-6">
      <RoleBadge role={user.role} />
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Admin portal required</h1>
      <p className="mt-3 text-gray-600">
        You are signed in as <strong>{user.firstName}</strong> ({user.role.replaceAll('_', ' ')}).
        Staff accounts use the StayNest Admin Portal — not the customer site.
      </p>

      {user.role === 'PROPERTY_MANAGER' && (
        <p className="mt-3 text-sm text-gray-500">
          Approve or reject host listings under <strong>Listing Approval</strong> in the admin portal.
        </p>
      )}

      <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
        <a
          href={showListingApproval ? listingApprovalUrl() : ADMIN_PORTAL_URL}
          className="btn-primary"
        >
          {showListingApproval ? 'Open Listing Approval' : 'Open Admin Portal'}
        </a>
        <button type="button" className="btn-secondary" onClick={() => void logout()}>
          Log out
        </button>
      </div>

      <p className="mt-6 text-xs text-gray-400">
        Admin portal: {ADMIN_PORTAL_URL}
      </p>
    </div>
  );
}
