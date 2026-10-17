import type { ConnectionState } from '../hooks/useScoreStream.ts'

const LABELS: Record<ConnectionState, string> = {
  connecting: 'Connecting…',
  live: 'Live',
  reconnecting: 'Reconnecting…',
}

interface ConnectionIndicatorProps {
  state: ConnectionState
}

function ConnectionIndicator({ state }: ConnectionIndicatorProps) {
  return (
    <span className={`connection connection-${state}`} role="status">
      <span className="connection-dot" aria-hidden="true" />
      {LABELS[state]}
    </span>
  )
}

export default ConnectionIndicator
