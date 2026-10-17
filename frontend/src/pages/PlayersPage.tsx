import { Link } from 'react-router-dom'
import { fetchPlayers } from '../api.ts'
import { Skeleton } from '../components/Skeleton.tsx'
import StateMessage, { ErrorState } from '../components/StateMessage.tsx'
import TeamMark from '../components/TeamMark.tsx'
import { useFetch } from '../hooks/useFetch.ts'
import { roleLabel, teamStyle } from '../teams.ts'
import type { PlayerResponse, TeamResponse } from '../types.ts'

interface TeamGroup {
  team: TeamResponse
  players: PlayerResponse[]
}

// The API lists players in squad order, so a team's players stay in batting order.
function groupByTeam(players: PlayerResponse[]): TeamGroup[] {
  const groups = new Map<number, TeamGroup>()
  for (const player of players) {
    const group = groups.get(player.team.id) ?? { team: player.team, players: [] }
    group.players.push(player)
    groups.set(player.team.id, group)
  }
  return [...groups.values()]
}

function PlayersPage() {
  const { data, error, loading, retry } = useFetch(fetchPlayers, [])

  return (
    <>
      <header className="page-header">
        <h1>Players</h1>
        <p className="muted">Pick a player to see career statistics.</p>
      </header>

      {loading && <PlayersSkeleton />}
      {error && <ErrorState message={error} onRetry={retry} />}
      {data && data.length === 0 && (
        <StateMessage icon="👥" title="No players yet" text="Players will show up here once teams are added." />
      )}
      {data && groupByTeam(data).map((group) => <TeamSection key={group.team.id} group={group} />)}
    </>
  )
}

function TeamSection({ group }: { group: TeamGroup }) {
  return (
    <section className="team-section" style={teamStyle(group.team.shortName)} aria-label={group.team.name}>
      <h2 className="section-title team-title">
        <TeamMark shortName={group.team.shortName} />
        {group.team.name}
      </h2>
      <ul className="player-grid">
        {group.players.map((player) => (
          <li key={player.id}>
            <Link to={`/players/${player.id}`} className="card player-card">
              <span className="player-card-name">{player.name}</span>
              <span className="role-tag">{roleLabel(player.role)}</span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}

function PlayersSkeleton() {
  return (
    <div className="stack" role="status" aria-label="Loading">
      <Skeleton height={28} width="30%" />
      <div className="player-grid">
        {Array.from({ length: 8 }, (_, index) => (
          <Skeleton key={index} height={64} />
        ))}
      </div>
    </div>
  )
}

export default PlayersPage
