import { fetchScorecard } from '../api.ts'
import { useFetch } from '../hooks/useFetch.ts'
import { teamStyle } from '../teams.ts'
import type { InningsScorecard, MatchStatus } from '../types.ts'
import { ErrorState } from './StateMessage.tsx'
import { SkeletonPanel } from './Skeleton.tsx'
import StateMessage from './StateMessage.tsx'
import TeamMark from './TeamMark.tsx'

const REFRESH_MS = 3000

interface ScorecardPanelProps {
  matchId: number
  status: MatchStatus
}

function ScorecardPanel({ matchId, status }: ScorecardPanelProps) {
  const { data, error, loading, retry } = useFetch(
    () => fetchScorecard(matchId),
    [matchId, status],
    status === 'LIVE' ? REFRESH_MS : undefined,
  )

  if (loading) return <SkeletonPanel />
  if (error || !data) return <ErrorState message={error ?? 'No data'} onRetry={retry} />
  if (data.innings.length === 0) {
    return <StateMessage icon="📋" title="No scorecard yet" text="It appears once the first ball is bowled." />
  }

  return (
    <div className="stack">
      {data.innings.map((innings) => (
        <InningsCard key={innings.inningsNumber} innings={innings} />
      ))}
    </div>
  )
}

function InningsCard({ innings }: { innings: InningsScorecard }) {
  const team = innings.battingTeam
  return (
    <article className="card innings-card" style={teamStyle(team.shortName)}>
      <header className="innings-header">
        <h3>
          <TeamMark shortName={team.shortName} />
          {team.name}
        </h3>
        <p className="innings-total">
          {innings.runs}/{innings.wickets} <span className="muted">({innings.overs} ov)</span>
        </p>
      </header>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Batter</th>
              <th className="num">R</th>
              <th className="num">B</th>
              <th className="num">4s</th>
              <th className="num">6s</th>
              <th className="num">SR</th>
            </tr>
          </thead>
          <tbody>
            {innings.batting.map((row) => (
              <tr key={row.playerId}>
                <td>
                  <span className="player-name">{row.name}</span>
                  <span className={row.dismissal === 'not out' ? 'dismissal not-out' : 'dismissal'}>
                    {row.dismissal}
                  </span>
                </td>
                <td className="num strong">{row.runs}</td>
                <td className="num">{row.balls}</td>
                <td className="num">{row.fours}</td>
                <td className="num">{row.sixes}</td>
                <td className="num">{row.strikeRate.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <p className="extras">
        <span className="muted">Extras</span> {innings.extras.total}{' '}
        <span className="muted">
          (wides {innings.extras.wides}, no-balls {innings.extras.noBalls})
        </span>
      </p>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Bowler</th>
              <th className="num">O</th>
              <th className="num">R</th>
              <th className="num">W</th>
              <th className="num">Econ</th>
            </tr>
          </thead>
          <tbody>
            {innings.bowling.map((row) => (
              <tr key={row.playerId}>
                <td className="player-name">{row.name}</td>
                <td className="num">{row.overs}</td>
                <td className="num">{row.runs}</td>
                <td className="num strong">{row.wickets}</td>
                <td className="num">{row.economy.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </article>
  )
}

export default ScorecardPanel
