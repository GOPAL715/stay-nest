interface PageLoaderProps {
  message?: string;
  fullScreen?: boolean;
}

export function PageLoader({ message = 'Loading…', fullScreen = false }: PageLoaderProps) {
  return (
    <div
      className={
        fullScreen
          ? 'flex h-screen flex-col items-center justify-center gap-3 bg-white'
          : 'flex min-h-[40vh] flex-col items-center justify-center gap-3 py-16'
      }
    >
      <div
        className="h-8 w-8 animate-spin rounded-full border-2 border-primary-600 border-t-transparent"
        aria-hidden="true"
      />
      <p className="text-sm text-gray-500">{message}</p>
    </div>
  );
}
