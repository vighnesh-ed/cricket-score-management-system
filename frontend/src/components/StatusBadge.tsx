import type { MatchStatus } from '../types.ts'

interface StatusBadgeProps {
  status: MatchStatus
}

function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`badge badge-${status.toLowerCase()}`}>
      {status === 'LIVE' && <span className="pulse-dot" aria-hidden="true" />}
      {status}
    </span>
  )
}

export default StatusBadge
