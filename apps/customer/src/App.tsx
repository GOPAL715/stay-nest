import {
  createRouter,
  createRootRoute,
  createRoute,
  RouterProvider,
  Outlet,
  Link,
  Navigate,
} from '@tanstack/react-router';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { PageLoader } from './components/PageLoader';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { SearchPage } from './pages/SearchPage';
import { PropertyDetailPage } from './pages/PropertyDetailPage';
import { MyTripsPage } from './pages/MyTripsPage';
import { ForgotPasswordPage } from './pages/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/ResetPasswordPage';
import { VerifyEmailPage } from './pages/VerifyEmailPage';
import { VerifyOtpPage } from './pages/VerifyOtpPage';
import { BecomeHostPage } from './pages/BecomeHostPage';
import { MyListingsPage } from './pages/MyListingsPage';
import { ListingCreatePage } from './pages/ListingCreatePage';
import { ListingEditPage } from './pages/ListingEditPage';
import { HostBookingsPage } from './pages/HostBookingsPage';
import { CalendarPage } from './pages/CalendarPage';
import { TripDetailPage } from './pages/TripDetailPage';
import { StaffPortalPage } from './pages/StaffPortalPage';
import { NavLink } from './components/NavLink';
import { RoleBadge } from './components/RoleBadge';
import { isStaffRole } from './utils/roles';

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isLoggedIn, isRestoring } = useAuth();
  if (isRestoring) {
    return <PageLoader message="Restoring session…" fullScreen />;
  }
  if (!isLoggedIn) {
    return <Navigate to="/login" />;
  }
  return <>{children}</>;
}

function RequireHost({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  if (isStaffRole(user?.role)) {
    return <Navigate to="/staff-portal" />;
  }
  if (user?.role !== 'HOST') {
    return <Navigate to="/become-host" />;
  }
  return <>{children}</>;
}

function RequireCustomer({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  if (isStaffRole(user?.role)) {
    return <Navigate to="/staff-portal" />;
  }
  return <>{children}</>;
}

function RequireGuest({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  if (isStaffRole(user?.role)) {
    return <Navigate to="/staff-portal" />;
  }
  if (user?.role === 'HOST') {
    return <Navigate to="/search" />;
  }
  return <>{children}</>;
}

function NavBar() {
  const { logout, user } = useAuth();

  return (
    <header className="border-b border-gray-200 bg-white shadow-sm">
      <nav className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-2">
          <Link to="/" className="text-xl font-bold text-primary-600">
            StayNest
          </Link>
          {user && <RoleBadge role={user.role} />}
        </div>
        <div className="flex items-center gap-4">
          {isStaffRole(user?.role) ? (
            <NavLink to="/staff-portal">Admin Portal</NavLink>
          ) : (
            <>
              <NavLink to="/search">Search</NavLink>
              {user?.role === 'GUEST' && (
                <>
                  <NavLink to="/become-host">Become a Host</NavLink>
                  <NavLink to="/trips">My Trips</NavLink>
                </>
              )}
              {user?.role === 'HOST' && (
                <>
                  <NavLink to="/listings">My Listings</NavLink>
                  <NavLink to="/booking-requests">Booking Requests</NavLink>
                  <NavLink to="/calendar">Calendar</NavLink>
                </>
              )}
            </>
          )}
          <div className="flex items-center gap-3">
            {user && (
              <span className="text-sm text-gray-600 hidden sm:inline">{user.firstName}</span>
            )}
            <button type="button" onClick={() => void logout()} className="btn-secondary text-sm">
              Log out
            </button>
          </div>
        </div>
      </nav>
    </header>
  );
}

function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <Link to="/login" className="text-xl font-bold text-primary-600">
          StayNest
        </Link>
      </div>
      {children}
    </div>
  );
}

function AppShell() {
  return (
    <RequireAuth>
      <div className="min-h-screen flex flex-col">
        <NavBar />
        <main className="flex-1">
          <Outlet />
        </main>
        <footer className="border-t border-gray-200 bg-gray-50 py-6 text-center text-sm text-gray-500">
          © {new Date().getFullYear()} StayNest. All rights reserved.
        </footer>
      </div>
    </RequireAuth>
  );
}

const rootRoute = createRootRoute({
  component: Outlet,
});

const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/login',
  component: () => (
    <AuthLayout>
      <LoginPage />
    </AuthLayout>
  ),
});

const registerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/register',
  component: () => (
    <AuthLayout>
      <RegisterPage />
    </AuthLayout>
  ),
});

const forgotPasswordRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/forgot-password',
  component: () => (
    <AuthLayout>
      <ForgotPasswordPage />
    </AuthLayout>
  ),
});

const resetPasswordRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/reset-password',
  validateSearch: (search: Record<string, unknown>) => ({
    token: typeof search.token === 'string' ? search.token : '',
  }),
  component: () => (
    <AuthLayout>
      <ResetPasswordPage />
    </AuthLayout>
  ),
});

const verifyEmailRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/verify-email',
  validateSearch: (search: Record<string, unknown>) => ({
    token: typeof search.token === 'string' ? search.token : '',
  }),
  component: () => (
    <AuthLayout>
      <VerifyEmailPage />
    </AuthLayout>
  ),
});

const verifyOtpRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/verify-otp',
  validateSearch: (search: Record<string, unknown>) => ({
    email: typeof search.email === 'string' ? search.email : '',
  }),
  component: () => (
    <AuthLayout>
      <VerifyOtpPage />
    </AuthLayout>
  ),
});

const appRoute = createRoute({
  getParentRoute: () => rootRoute,
  id: 'app',
  component: AppShell,
});

const indexRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/',
  component: () => (
    <RequireCustomer>
      <section className="mx-auto max-w-7xl px-4 py-20 text-center sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl">
          Find your next stay
        </h1>
        <p className="mt-4 text-lg text-gray-600">
          Discover unique places to stay from local hosts around the world.
        </p>
        <div className="mt-8 flex justify-center gap-4">
          <Link to="/search" className="btn-primary px-6 py-3 text-base">
            Start searching
          </Link>
        </div>
      </section>
    </RequireCustomer>
  ),
});

const staffPortalRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/staff-portal',
  component: StaffPortalPage,
});

const searchRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/search',
  component: () => (
    <RequireCustomer>
      <SearchPage />
    </RequireCustomer>
  ),
});

const tripsRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/trips',
  component: () => (
    <RequireGuest>
      <MyTripsPage />
    </RequireGuest>
  ),
});

const tripDetailRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/trips/$bookingId',
  component: () => {
    const { bookingId } = tripDetailRoute.useParams();
    return (
      <RequireGuest>
        <TripDetailPage bookingId={bookingId} />
      </RequireGuest>
    );
  },
});

const becomeHostRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/become-host',
  component: () => (
    <RequireCustomer>
      <BecomeHostPage />
    </RequireCustomer>
  ),
});

const myListingsRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/listings',
  component: () => (
    <RequireHost>
      <MyListingsPage />
    </RequireHost>
  ),
});

const listingCreateRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/listings/new',
  component: () => (
    <RequireHost>
      <ListingCreatePage />
    </RequireHost>
  ),
});

const listingEditRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/listings/$listingId/edit',
  component: () => {
    const { listingId } = listingEditRoute.useParams();
    return (
      <RequireHost>
        <ListingEditPage listingId={listingId} />
      </RequireHost>
    );
  },
});

const hostBookingsRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/booking-requests',
  component: () => (
    <RequireHost>
      <HostBookingsPage />
    </RequireHost>
  ),
});

const calendarRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/calendar',
  component: () => (
    <RequireHost>
      <CalendarPage />
    </RequireHost>
  ),
});

const propertyRoute = createRoute({
  getParentRoute: () => appRoute,
  path: '/properties/$propertyId',
  component: () => {
    const { propertyId } = propertyRoute.useParams();
    return (
      <RequireCustomer>
        <PropertyDetailPage propertyId={propertyId} />
      </RequireCustomer>
    );
  },
});

const routeTree = rootRoute.addChildren([
  loginRoute,
  registerRoute,
  forgotPasswordRoute,
  resetPasswordRoute,
  verifyEmailRoute,
  verifyOtpRoute,
  appRoute.addChildren([
    indexRoute,
    staffPortalRoute,
    searchRoute,
    tripsRoute,
    tripDetailRoute,
    becomeHostRoute,
    myListingsRoute,
    listingCreateRoute,
    listingEditRoute,
    hostBookingsRoute,
    calendarRoute,
    propertyRoute,
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
