import type {
  ErrorResponse,
  MatchResponse,
  MatchSummaryResponse,
  PlayerResponse,
  PlayerStatsResponse,
  ScoreResponse,
  ScorecardResponse,
} from './types.ts'

export const API_BASE_URL = 'http://localhost:8080'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, init)
  const body: unknown = await response.json()
  if (!response.ok) {
    throw new Error((body as ErrorResponse).message)
  }
  return body as T
}

export function fetchMatches(): Promise<MatchResponse[]> {
  return request<MatchResponse[]>('/api/matches')
}

export function fetchScorecard(matchId: number): Promise<ScorecardResponse> {
  return request<ScorecardResponse>(`/api/matches/${matchId}/scorecard`)
}

export function fetchSummary(matchId: number): Promise<MatchSummaryResponse> {
  return request<MatchSummaryResponse>(`/api/matches/${matchId}/summary`)
}

export function fetchPlayers(): Promise<PlayerResponse[]> {
  return request<PlayerResponse[]>('/api/players')
}

export function fetchPlayerStats(playerId: number): Promise<PlayerStatsResponse> {
  return request<PlayerStatsResponse>(`/api/players/${playerId}/stats`)
}

export function startMatch(matchId: number): Promise<ScoreResponse> {
  return request<ScoreResponse>(`/api/matches/${matchId}/start`, { method: 'POST' })
}

export interface ScoreStreamHandlers {
  onScore: (score: ScoreResponse) => void
  onOpen: () => void
  onError: () => void
}

// The server sends the current score as soon as the stream opens, then a new one after every ball.
// The caller must close the returned EventSource when it is done with it.
export function openScoreStream(matchId: number, handlers: ScoreStreamHandlers): EventSource {
  const source = new EventSource(`${API_BASE_URL}/api/matches/${matchId}/stream`)
  source.addEventListener('score', (event: MessageEvent<string>) => {
    const score: unknown = JSON.parse(event.data)
    handlers.onScore(score as ScoreResponse)
  })
  source.addEventListener('open', handlers.onOpen)
  source.addEventListener('error', handlers.onError)
  return source
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error)
}
