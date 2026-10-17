import type { CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import { useScoreStream } from '../hooks/useScoreStream.ts'
import { formatStart, teamColor } from '../teams.ts'
import type { MatchResponse } from '../types.ts'
import StatusBadge from './StatusBadge.tsx'
import TeamMark from './TeamMark.tsx'

interface MatchCardProps {
  match: MatchResponse
}

function MatchCard({ match }: MatchCardProps) {
  const style = {
    '--team-a': teamColor(match.teamA.shortName),
    '--team-b': teamColor(match.teamB.shortName),
  } as CSSProperties

  return (
    <Link to={`/matches/${match.id}`} className="card match-card" style={style}>
      <div className="match-card-top">
        <span className="muted">{match.totalOvers} overs</span>
        <StatusBadge status={match.status} />
      </div>
      <div className="match-card-teams">
        <div className="match-card-team">
          <TeamMark shortName={match.teamA.shortName} />
          <span>{match.teamA.name}</span>
        </div>
        <span className="versus">vs</span>
        <div className="match-card-team">
          <TeamMark shortName={match.teamB.shortName} />
          <span>{match.teamB.name}</span>
        </div>
      </div>
      <p className="muted">{match.venue}</p>
      <div className="match-card-footer">
        {match.status === 'LIVE' && <LiveScore matchId={match.id} />}
        {match.status === 'COMPLETED' && <p className="match-result">{match.result}</p>}
        {match.status === 'SCHEDULED' && <p className="muted">Starts {formatStart(match.startTime)}</p>}
      </div>
    </Link>
  )
}

// Each live card follows its own match. The stream closes when the card unmounts.
function LiveScore({ matchId }: { matchId: number }) {
  const { score } = useScoreStream(matchId)
  if (!score?.battingTeam) {
    return <p className="muted">Loading score…</p>
  }
  return (
    <p className="live-score">
      <span className="live-score-team">{score.battingTeam.shortName}</span>
      <span className="live-score-runs">
        {score.runs}/{score.wickets}
      </span>
      <span className="muted">({score.overs} ov)</span>
      {score.runsRequired !== null && <span className="live-score-chase">need {score.runsRequired}</span>}
    </p>
  )
}

export default MatchCard
