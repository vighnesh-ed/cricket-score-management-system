import { Link, useParams } from 'react-router-dom'
import { fetchPlayerStats } from '../api.ts'
import { SkeletonPanel } from '../components/Skeleton.tsx'
import StateMessage, { ErrorState } from '../components/StateMessage.tsx'
import TeamMark from '../components/TeamMark.tsx'
import { useFetch } from '../hooks/useFetch.ts'
import { teamStyle } from '../teams.ts'

function PlayerPage() {
  const playerId = Number(useParams().id)
  if (!Number.isInteger(playerId)) {
    return <PlayerNotFound />
  }
  return <PlayerStats key={playerId} playerId={playerId} />
}

function PlayerNotFound() {
  return (
    <StateMessage icon="🔍" title="Player not found" text="This player does not exist.">
      <Link to="/players" className="button">
        Back to players
      </Link>
    </StateMessage>
  )
}

function PlayerStats({ playerId }: { playerId: number }) {
  const { data, error, loading, retry } = useFetch(() => fetchPlayerStats(playerId), [playerId])

  if (loading) return <SkeletonPanel />
  if (error || !data) {
    return error?.includes('not found') ? (
      <PlayerNotFound />
    ) : (
      <ErrorState message={error ?? 'No data'} onRetry={retry} />
    )
  }

  return (
    <>
      <Link to="/players" className="back-link">
        ← All players
      </Link>
      <article className="card stats-card" style={teamStyle(data.team.shortName)}>
        <header className="stats-header">
          <TeamMark shortName={data.team.shortName} />
          <div>
            <h1>{data.name}</h1>
            <p className="muted">{data.team.name}</p>
          </div>
          <div className="stats-matches">
            <span className="stat-value">{data.matches}</span>
            <span className="stat-label">{data.matches === 1 ? 'Match' : 'Matches'}</span>
          </div>
        </header>

        <h2 className="panel-title">Batting</h2>
        <dl className="stat-grid">
          <Stat label="Runs" value={data.runs} big />
          <Stat label="Balls" value={data.balls} />
          <Stat label="Strike rate" value={data.strikeRate.toFixed(2)} />
          <Stat label="Fours" value={data.fours} />
          <Stat label="Sixes" value={data.sixes} />
        </dl>

        <h2 className="panel-title">Bowling</h2>
        <dl className="stat-grid">
          <Stat label="Wickets" value={data.wickets} big />
          <Stat label="Overs" value={data.oversBowled} />
          <Stat label="Runs conceded" value={data.runsConceded} />
          <Stat label="Economy" value={data.economy.toFixed(2)} />
        </dl>

        {data.matches === 0 && (
          <p className="muted stats-note">No matches played yet. Start a match to see figures here.</p>
        )}
      </article>
    </>
  )
}

function Stat({ label, value, big = false }: { label: string; value: string | number; big?: boolean }) {
  return (
    <div className={big ? 'stat stat-big' : 'stat'}>
      <dt className="stat-label">{label}</dt>
      <dd className="stat-value">{value}</dd>
    </div>
  )
}

export default PlayerPage
