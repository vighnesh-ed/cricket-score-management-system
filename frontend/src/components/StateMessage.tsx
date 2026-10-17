import type { ReactNode } from 'react'

interface StateMessageProps {
  children?: ReactNode
  icon: string
  title: string
  text?: string
  onRetry?: () => void
}

// Friendly full-width message for empty and error states.
function StateMessage({ icon, title, text, onRetry, children }: StateMessageProps) {
  return (
    <div className="state-message" role="status">
      <span className="state-icon" aria-hidden="true">
        {icon}
      </span>
      <h3>{title}</h3>
      {text && <p className="muted">{text}</p>}
      {children}
      {onRetry && (
        <button type="button" className="button" onClick={onRetry}>
          Try again
        </button>
      )}
    </div>
  )
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <StateMessage
      icon="⚠️"
      title="Something went wrong"
      text={`${message}. Check that the backend is running on port 8080.`}
      onRetry={onRetry}
    />
  )
}

export default StateMessage
