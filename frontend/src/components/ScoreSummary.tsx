import { teamStyle } from '../teams.ts'
import type { ScoreResponse, TeamResponse } from '../types.ts'
import TeamMark from './TeamMark.tsx'

interface ScoreSummaryProps {
  score: ScoreResponse
  team: TeamResponse
}

function ScoreSummary({ score, team }: ScoreSummaryProps) {
  const chasing = score.status === 'LIVE' && score.target !== null

  return (
    <div className="score-summary" style={teamStyle(team.shortName)}>
      <p className="innings-label">
        <TeamMark shortName={team.shortName} />
        {team.name} · {score.inningsNumber === 1 ? '1st' : '2nd'} innings
      </p>
      <p className="score-line">
        <span className="score-runs">
          {score.runs}/{score.wickets}
        </span>
        <span className="score-overs">({score.overs} ov)</span>
      </p>
      <p className="muted">Run rate {score.runRate.toFixed(2)}</p>
      {chasing && (
        <p className="chase">
          Target {score.target} · Need {score.runsRequired} {score.runsRequired === 1 ? 'run' : 'runs'}
        </p>
      )}
    </div>
  )
}

export default ScoreSummary
