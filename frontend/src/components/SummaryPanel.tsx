import { Link } from 'react-router-dom'
import { fetchSummary } from '../api.ts'
import { useFetch } from '../hooks/useFetch.ts'
import { teamColor, teamStyle } from '../teams.ts'
import type { MatchStatus, MatchSummaryResponse } from '../types.ts'
import BarChart from './BarChart.tsx'
import { SkeletonPanel } from './Skeleton.tsx'
import StateMessage, { ErrorState } from './StateMessage.tsx'
import TeamMark from './TeamMark.tsx'

const REFRESH_MS = 3000

interface SummaryPanelProps {
  matchId: number
  status: MatchStatus
}

function SummaryPanel({ matchId, status }: SummaryPanelProps) {
  const { data, error, loading, retry } = useFetch(
    () => fetchSummary(matchId),
    [matchId, status],
    status === 'LIVE' ? REFRESH_MS : undefined,
  )

  if (loading) return <SkeletonPanel />
  if (error || !data) return <ErrorState message={error ?? 'No data'} onRetry={retry} />
  if (data.innings.length === 0) {
    return <StateMessage icon="📊" title="No summary yet" text="It appears once the first ball is bowled." />
  }

  return (
    <div className="stack">
      <ResultBanner summary={data} />
      <div className="innings-tiles">
        {data.innings.map((innings) => (
          <div key={innings.inningsNumber} className="card tile" style={teamStyle(innings.battingTeam.shortName)}>
            <p className="tile-label">
              <TeamMark shortName={innings.battingTeam.shortName} />
              {innings.battingTeam.name}
            </p>
            <p className="tile-score">
              {innings.runs}/{innings.wickets}
            </p>
            <p className="muted">{innings.overs} overs</p>
          </div>
        ))}
      </div>

      {data.playerOfTheMatch && <PlayerOfTheMatchCard player={data.playerOfTheMatch} status={data.status} />}

      <div className="two-columns">
        <article className="card">
          <h3 className="panel-title">Top batters</h3>
          <ol className="leaderboard">
            {data.topBatters.map((row) => (
              <li key={row.playerId}>
                <Link to={`/players/${row.playerId}`} className="leader-link" style={teamStyle(row.team)}>
                  <span className="player-name">{row.name}</span>
                  <span className="muted">{row.team}</span>
                  <span className="leader-figures">
                    {row.runs} <span className="muted">({row.balls})</span>
                  </span>
                </Link>
              </li>
            ))}
          </ol>
        </article>
        <article className="card">
          <h3 className="panel-title">Top bowlers</h3>
          <ol className="leaderboard">
            {data.topBowlers.map((row) => (
              <li key={row.playerId}>
                <Link to={`/players/${row.playerId}`} className="leader-link" style={teamStyle(row.team)}>
                  <span className="player-name">{row.name}</span>
                  <span className="muted">{row.team}</span>
                  <span className="leader-figures">
                    {row.wickets}/{row.runs} <span className="muted">({row.overs})</span>
                  </span>
                </Link>
              </li>
            ))}
          </ol>
        </article>
      </div>

      <div className="two-columns">
        {data.innings.map((innings) => (
          <article key={innings.inningsNumber} className="card">
            <BarChart
              title={`${innings.battingTeam.name} runs per over`}
              values={innings.runsPerOver}
              color={teamColor(innings.battingTeam.shortName)}
            />
          </article>
        ))}
      </div>
    </div>
  )
}

function ResultBanner({ summary }: { summary: MatchSummaryResponse }) {
  if (summary.status === 'COMPLETED' && summary.result) {
    return <p className="result-banner">{summary.result}</p>
  }
  return <p className="result-banner pending">Match in progress, figures update live.</p>
}

function PlayerOfTheMatchCard({
  player,
  status,
}: {
  player: NonNullable<MatchSummaryResponse['playerOfTheMatch']>
  status: MatchStatus
}) {
  const parts = [
    player.runs > 0 ? `${player.runs} ${player.runs === 1 ? 'run' : 'runs'}` : null,
    player.wickets > 0 ? `${player.wickets} ${player.wickets === 1 ? 'wicket' : 'wickets'}` : null,
  ].filter((part) => part !== null)

  return (
    <Link to={`/players/${player.playerId}`} className="card potm" style={teamStyle(player.team)}>
      <span className="potm-trophy" aria-hidden="true">
        🏆
      </span>
      <div>
        <p className="potm-label">{status === 'COMPLETED' ? 'Player of the match' : 'Player of the match so far'}</p>
        <p className="potm-name">{player.name}</p>
        <p className="muted">
          {player.team} · {parts.join(' and ')}
        </p>
      </div>
    </Link>
  )
}

export default SummaryPanel
