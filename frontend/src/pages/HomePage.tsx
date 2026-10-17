import { fetchMatches } from '../api.ts'
import MatchCard from '../components/MatchCard.tsx'
import { SkeletonCards } from '../components/Skeleton.tsx'
import StateMessage, { ErrorState } from '../components/StateMessage.tsx'
import { useFetch } from '../hooks/useFetch.ts'

// Matches start and finish while the page is open, so the list reloads quietly.
const REFRESH_MS = 10000

function HomePage() {
  const { data, error, loading, retry } = useFetch(fetchMatches, [], REFRESH_MS)
  const liveCount = data?.filter((match) => match.status === 'LIVE').length ?? 0

  return (
    <>
      <section className="hero">
        <p className="hero-kicker">Live cricket scoring</p>
        <h1>Follow every ball, as it happens</h1>
        <p className="hero-text">
          Ongoing matches, ball-by-ball scores, scorecards and player statistics in one dashboard.
        </p>
        {liveCount > 0 && (
          <p className="hero-live">
            <span className="pulse-dot" aria-hidden="true" />
            {liveCount} {liveCount === 1 ? 'match' : 'matches'} live now
          </p>
        )}
      </section>

      <section aria-labelledby="matches-title">
        <h2 id="matches-title" className="section-title">
          Matches
        </h2>
        {loading && <SkeletonCards />}
        {error && !data && <ErrorState message={error} onRetry={retry} />}
        {data && data.length === 0 && (
          <StateMessage icon="🏏" title="No matches yet" text="Matches will show up here once they are scheduled." />
        )}
        {data && data.length > 0 && (
          <ul className="card-grid">
            {data.map((match) => (
              <li key={match.id}>
                <MatchCard match={match} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}

export default HomePage
