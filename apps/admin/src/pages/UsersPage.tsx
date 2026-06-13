import { useQuery } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, PageResult, UserProfile } from '../types/api';

export function UsersPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-users'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/admin/users?page=0&size=20');
      if (!res.ok) throw new Error('Failed to load users');
      const json = (await res.json()) as ApiEnvelope<PageResult<UserProfile>>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading users…" />;
  }

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Users</h2>
      <p className="mt-1 text-sm text-gray-500">Registered platform users.</p>
      {isError && <p className="mt-6 text-red-600">Failed to load users.</p>}
      <ul className="mt-6 space-y-3">
        {data?.content.map((user) => (
          <li key={user.id} className="card flex items-center justify-between">
            <div>
              <p className="font-medium text-gray-900">{user.firstName} {user.lastName}</p>
              <p className="text-sm text-gray-500">{user.email}</p>
            </div>
            <div className="text-right">
              <span className="badge-gray">{user.role}</span>
              <p className="mt-1 text-xs text-gray-500">{user.status}</p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
