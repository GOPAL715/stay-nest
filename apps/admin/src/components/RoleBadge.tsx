const ROLE_STYLES: Record<string, { label: string; className: string }> = {
  GUEST: { label: 'Guest', className: 'bg-slate-100 text-slate-700' },
  HOST: { label: 'Host', className: 'bg-emerald-100 text-emerald-800' },
  SUPER_ADMIN: { label: 'Admin', className: 'bg-purple-100 text-purple-800' },
  PROPERTY_MANAGER: { label: 'Property Manager', className: 'bg-blue-100 text-blue-800' },
  SUPPORT_AGENT: { label: 'Support Agent', className: 'bg-amber-100 text-amber-800' },
};

interface RoleBadgeProps {
  role: string;
}

export function RoleBadge({ role }: RoleBadgeProps) {
  const config = ROLE_STYLES[role] ?? {
    label: role.replaceAll('_', ' '),
    className: 'bg-gray-100 text-gray-700',
  };

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${config.className}`}
    >
      {config.label}
    </span>
  );
}
