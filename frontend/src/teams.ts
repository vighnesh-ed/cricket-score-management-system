import type { CSSProperties } from 'react'
import type { PlayerRole } from './types.ts'

const TEAM_COLORS: Record<string, string> = {
  IND: '#3b82f6',
  AUS: '#facc15',
  ENG: '#f43f5e',
  RSA: '#22c55e',
}

const FALLBACK_COLOR = '#a78bfa'

export function teamColor(shortName: string): string {
  return TEAM_COLORS[shortName] ?? FALLBACK_COLOR
}

// Components read the colour from the --team CSS variable for their accents.
export function teamStyle(shortName: string): CSSProperties {
  return { '--team': teamColor(shortName) } as CSSProperties
}

const ROLE_LABELS: Record<PlayerRole, string> = {
  BATSMAN: 'Batter',
  BOWLER: 'Bowler',
  ALL_ROUNDER: 'All-rounder',
  WICKET_KEEPER: 'Wicket-keeper',
}

export function roleLabel(role: PlayerRole): string {
  return ROLE_LABELS[role]
}

export function formatStart(startTime: string): string {
  return new Date(startTime).toLocaleString(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}
