import {
  createRouter,
  createRootRoute,
  createRoute,
  RouterProvider,
  Outlet,
  Link,
  useRouterState,
  Navigate,
} from '@tanstack/react-router';
import { PageLoader } from './components/PageLoader';
import { RoleBadge } from './components/RoleBadge';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { AdminLoginPage } from './pages/AdminLoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { ListingsPage } from './pages/ListingsPage';
import { BookingsPage } from './pages/BookingsPage';
import { UsersPage } from './pages/UsersPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { ConfigPage } from './pages/ConfigPage';
import { CreateAdminPage } from './pages/CreateAdminPage';
import { hasRole, navItemsForRole } from './utils/roles';

function RequireRole({
  children,
  allowed,
}: {
  children: React.ReactNode;
  allowed: readonly string[];
}) {
  const { user } = useAuth();
  if (!hasRole(user?.role, allowed)) {
    return <Navigate to="/dashboard" />;
  }
  return <>{children}</>;
}

function Sidebar() {
  const routerState = useRouterState();
  const currentPath = routerState.location.pathname;
  const { logout, user } = useAuth();

  return (
    <aside className="flex h-full w-60 flex-col border-r border-gray-200 bg-white">
      <div className="flex h-16 items-center gap-2 border-b border-gray-200 px-6">
        <Link to="/dashboard" className="text-xl font-bold text-primary-600">
          StayNest
        </Link>
        {user && <RoleBadge role={user.role} />}
      </div>
      {user && (
        <div className="border-b border-gray-200 px-4 py-3">
          <p className="truncate text-sm font-medium text-gray-900">
            {user.firstName} {user.lastName}
          </p>
        </div>
      )}
      <nav className="flex-1 overflow-y-auto px-3 py-4">
        <ul className="space-y-1">
          {navItemsForRole(user?.role).map(({ label, path, icon }) => {
            const isActive = currentPath === path || currentPath.startsWith(path + '/');
            return (
              <li key={path}>
                <Link
                  to={path}
                  className={[
                    'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
                  ].join(' ')}
                >
                  <span aria-hidden="true">{icon}</span>
                  {label}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
      <div className="border-t border-gray-200 p-4">
        <button
          type="button"
          onClick={() => void logout()}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50"
        >
          <span aria-hidden="true">🚪</span>
          Log out
        </button>
      </div>
    </aside>
  );
}

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isLoggedIn, isRestoring, user } = useAuth();
  if (isRestoring) {
    return <PageLoader message="Restoring session…" fullScreen />;
  }
  const allowedRoles = ['SUPER_ADMIN', 'PROPERTY_MANAGER', 'SUPPORT_AGENT'];
  if (!isLoggedIn || !user || !allowedRoles.includes(user.role)) {
    return <Navigate to="/login" />;
  }
  return <>{children}</>;
}

function AdminHeader() {
  const { user } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-6">
      <h1 className="text-sm font-semibold text-gray-500">StayNest Admin Portal</h1>
      {user && (
        <span className="text-sm text-gray-700">
          {user.firstName} {user.lastName}
        </span>
      )}
    </header>
  );
}

function AdminShell() {
  return (
    <RequireAuth>
      <div className="flex h-screen overflow-hidden bg-gray-100">
        <Sidebar />
        <div className="flex flex-1 flex-col overflow-hidden">
          <AdminHeader />
          <main className="flex-1 overflow-y-auto p-6">
            <Outlet />
          </main>
        </div>
      </div>
    </RequireAuth>
  );
}

const rootRoute = createRootRoute({ component: Outlet });

const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/login',
  component: AdminLoginPage,
});

const adminRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: 'admin',
  component: AdminShell,
});

const indexRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/',
  component: () => <Navigate to="/dashboard" />,
});

const dashboardRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/dashboard',
  component: DashboardPage,
});

const listingsRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/listings',
  component: () => (
    <RequireRole allowed={['SUPER_ADMIN', 'PROPERTY_MANAGER']}>
      <ListingsPage />
    </RequireRole>
  ),
});

const bookingsRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/bookings',
  component: () => (
    <RequireRole allowed={['SUPER_ADMIN', 'SUPPORT_AGENT']}>
      <BookingsPage />
    </RequireRole>
  ),
});

const usersRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/users',
  component: UsersPage,
});

const createAdminRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/users/create-admin',
  component: () => (
    <RequireRole allowed={['SUPER_ADMIN']}>
      <CreateAdminPage />
    </RequireRole>
  ),
});

const applicationsRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/applications',
  component: () => (
    <RequireRole allowed={['SUPER_ADMIN', 'PROPERTY_MANAGER']}>
      <ApplicationsPage />
    </RequireRole>
  ),
});

const configRoute = createRoute({
  getParentRoute: () => adminRoute,
  path: '/config',
  component: () => (
    <RequireRole allowed={['SUPER_ADMIN']}>
      <ConfigPage />
    </RequireRole>
  ),
});

const routeTree = rootRoute.addChildren([
  loginRoute,
  adminRoute.addChildren([
    indexRoute,
    dashboardRoute,
    listingsRoute,
    bookingsRoute,
    usersRoute,
    createAdminRoute,
    applicationsRoute,
    configRoute,
  ]),
]);

const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
