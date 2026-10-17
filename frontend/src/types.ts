// Mirrors the DTOs in backend/src/main/java/com/cricket/cms/dto.

export type MatchStatus = 'SCHEDULED' | 'LIVE' | 'COMPLETED'

export type BallOutcome =
  | 'DOT'
  | 'ONE'
  | 'TWO'
  | 'THREE'
  | 'FOUR'
  | 'SIX'
  | 'WICKET'
  | 'WIDE'
  | 'NO_BALL'

export interface TeamResponse {
  id: number
  name: string
  shortName: string
}

export interface MatchResponse {
  id: number
  teamA: TeamResponse
  teamB: TeamResponse
  venue: string
  totalOvers: number
  status: MatchStatus
  startTime: string
  result: string | null
}

export interface BatsmanScore {
  id: number
  name: string
  runs: number
  balls: number
}

export interface PlayerSummary {
  id: number
  name: string
}

// battingTeam, striker, nonStriker and bowler are null before the match starts.
// striker is also null once the batting side is all out.
// target and runsRequired are null in the first innings.
export interface ScoreResponse {
  matchId: number
  status: MatchStatus
  result: string | null
  inningsNumber: number
  battingTeam: TeamResponse | null
  runs: number
  wickets: number
  overs: string
  runRate: number
  striker: BatsmanScore | null
  nonStriker: BatsmanScore | null
  bowler: PlayerSummary | null
  lastSixBalls: string[]
  target: number | null
  runsRequired: number | null
}

export type PlayerRole = 'BATSMAN' | 'BOWLER' | 'ALL_ROUNDER' | 'WICKET_KEEPER'

export interface BattingRow {
  playerId: number
  name: string
  team: string
  runs: number
  balls: number
  fours: number
  sixes: number
  strikeRate: number
  // "not out" or "b <bowler>".
  dismissal: string
}

export interface BowlingRow {
  playerId: number
  name: string
  team: string
  overs: string
  runs: number
  wickets: number
  economy: number
}

export interface ExtrasResponse {
  wides: number
  noBalls: number
  total: number
}

export interface InningsScorecard {
  inningsNumber: number
  battingTeam: TeamResponse
  runs: number
  wickets: number
  overs: string
  extras: ExtrasResponse
  batting: BattingRow[]
  bowling: BowlingRow[]
  runsPerOver: number[]
}

export interface ScorecardResponse {
  matchId: number
  status: MatchStatus
  result: string | null
  innings: InningsScorecard[]
}

export interface InningsSummary {
  inningsNumber: number
  battingTeam: TeamResponse
  runs: number
  wickets: number
  overs: string
  runsPerOver: number[]
}

export interface PlayerOfTheMatch {
  playerId: number
  name: string
  team: string
  runs: number
  wickets: number
  impact: number
}

// playerOfTheMatch is null before the first ball.
export interface MatchSummaryResponse {
  matchId: number
  status: MatchStatus
  result: string | null
  innings: InningsSummary[]
  topBatters: BattingRow[]
  topBowlers: BowlingRow[]
  playerOfTheMatch: PlayerOfTheMatch | null
}

export interface PlayerResponse {
  id: number
  name: string
  role: PlayerRole
  team: TeamResponse
}

export interface PlayerStatsResponse {
  playerId: number
  name: string
  team: TeamResponse
  matches: number
  runs: number
  balls: number
  fours: number
  sixes: number
  strikeRate: number
  wickets: number
  oversBowled: string
  runsConceded: number
  economy: number
}

export interface BallRequest {
  outcome: BallOutcome
  batsmanId?: number | null
  bowlerId?: number | null
}

export interface ErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
