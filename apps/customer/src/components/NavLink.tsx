import { Link, type LinkProps } from '@tanstack/react-router';

const baseClass = 'text-sm font-medium text-gray-600 hover:text-primary-600 transition-colors';
const activeClass = 'text-sm font-medium text-primary-600 transition-colors';

interface NavLinkProps extends Omit<LinkProps, 'children'> {
  children: React.ReactNode;
  exact?: boolean;
}

export function NavLink({ children, exact = false, ...props }: NavLinkProps) {
  return (
    <Link
      {...props}
      className={baseClass}
      activeProps={{ className: activeClass }}
      activeOptions={{ exact }}
    >
      {children}
    </Link>
  );
}
