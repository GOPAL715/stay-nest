import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PageLoader } from '../components/PageLoader';
import { apiFetch } from '../services/apiFetch';
import type { ApiEnvelope, HostApplication, PageResult } from '../types/api';

export function ApplicationsPage() {
  const queryClient = useQueryClient();
  const [processingAction, setProcessingAction] = useState<{ id: string; type: 'approve' | 'reject' } | null>(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['host-applications'],
    queryFn: async () => {
      const res = await apiFetch('/api/v1/host-applications?page=0&size=20');
      if (!res.ok) throw new Error('Failed to load applications');
      const json = (await res.json()) as ApiEnvelope<PageResult<HostApplication>>;
      return json.data;
    },
  });

  if (isLoading) {
    return <PageLoader message="Loading applications…" />;
  }

  const approve = async (id: string) => {
    setProcessingAction({ id, type: 'approve' });
    try {
      await apiFetch(`/api/v1/host-applications/${id}/approve`, { method: 'PATCH' });
      await queryClient.invalidateQueries({ queryKey: ['host-applications'] });
    } catch (error) {
      console.error('Failed to approve application:', error);
    } finally {
      setProcessingAction(null);
    }
  };

  const reject = async (id: string) => {
    const reason = prompt('Please enter a rejection reason:', 'Does not meet host criteria at this time.');
    if (reason === null) return; // user cancelled

    setProcessingAction({ id, type: 'reject' });
    try {
      await apiFetch(`/api/v1/host-applications/${id}/reject`, {
        method: 'PATCH',
        body: JSON.stringify({ reason }),
      });
      await queryClient.invalidateQueries({ queryKey: ['host-applications'] });
    } catch (error) {
      console.error('Failed to reject application:', error);
    } finally {
      setProcessingAction(null);
    }
  };

  const hasApplications = data?.content && data.content.length > 0;

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900">Host Applications</h2>
      <p className="mt-1 text-sm text-gray-500">Review pending host applications.</p>
      {isError && <p className="mt-6 text-red-600">Failed to load applications.</p>}
      
      {!isError && !hasApplications && (
        <div className="mt-6 rounded-lg border-2 border-dashed border-gray-300 p-8 text-center text-gray-500">
          No host applications are currently pending review.
        </div>
      )}

      {hasApplications && (
        <ul className="mt-6 space-y-4">
          {data?.content.map((app) => (
            <li key={app.id} className="card">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <p className="font-semibold text-gray-900">
                    {app.applicant.firstName} {app.applicant.lastName}
                  </p>
                  <p className="text-sm text-gray-500">{app.applicant.email}</p>
                  <p className="mt-2 text-sm text-gray-700">{app.motivation}</p>
                  <span className="badge-yellow mt-2">{app.status}</span>
                </div>
                {app.status === 'PENDING' && (
                  <div className="flex gap-2">
                    <button
                      type="button"
                      className="btn-primary"
                      onClick={() => void approve(app.id)}
                      disabled={processingAction !== null}
                    >
                      {processingAction?.id === app.id && processingAction.type === 'approve' ? (
                        <>
                          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Approving...
                        </>
                      ) : (
                        'Approve'
                      )}
                    </button>
                    <button
                      type="button"
                      className="btn-danger"
                      onClick={() => void reject(app.id)}
                      disabled={processingAction !== null}
                    >
                      {processingAction?.id === app.id && processingAction.type === 'reject' ? (
                        <>
                          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Rejecting...
                        </>
                      ) : (
                        'Reject'
                      )}
                    </button>
                  </div>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
