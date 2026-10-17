interface SkeletonProps {
  height?: number
  width?: string
}

export function Skeleton({ height = 16, width = '100%' }: SkeletonProps) {
  return <div className="skeleton" style={{ height, width }} aria-hidden="true" />
}

export function SkeletonCards({ count = 3 }: { count?: number }) {
  return (
    <div className="card-grid" role="status" aria-label="Loading">
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="card skeleton-card">
          <Skeleton height={14} width="40%" />
          <Skeleton height={28} width="80%" />
          <Skeleton height={28} width="70%" />
          <Skeleton height={14} width="55%" />
        </div>
      ))}
    </div>
  )
}

export function SkeletonPanel() {
  return (
    <div className="card skeleton-card" role="status" aria-label="Loading">
      <Skeleton height={14} width="30%" />
      <Skeleton height={64} width="55%" />
      <Skeleton height={14} width="45%" />
      <Skeleton height={120} />
    </div>
  )
}
