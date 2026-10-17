import { useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { errorMessage, fetchMatches, startMatch } from '../api.ts'
import LivePanel from '../components/LivePanel.tsx'
import ScorecardPanel from '../components/ScorecardPanel.tsx'
import { SkeletonPanel } from '../components/Skeleton.tsx'
import StateMessage, { ErrorState } from '../components/StateMessage.tsx'
import StatusBadge from '../components/StatusBadge.tsx'
import SummaryPanel from '../components/SummaryPanel.tsx'
import Tabs from '../components/Tabs.tsx'
import TeamMark from '../components/TeamMark.tsx'
import { useFetch } from '../hooks/useFetch.ts'
import { useScoreStream } from '../hooks/useScoreStream.ts'
import { teamColor } from '../teams.ts'
import type { CSSProperties } from 'react'

type TabId = 'live' | 'scorecard' | 'summary'

const TABS = [
  { id: 'live', label: 'Live' },
  { id: 'scorecard', label: 'Scorecard' },
  { id: 'summary', label: 'Summary' },
] as const

function parseTab(value: string | null): TabId {
  return TABS.find((tab) => tab.id === value)?.id ?? 'live'
}

function MatchPage() {
  const matchId = Number(useParams().id)
  if (!Number.isInteger(matchId)) {
    return <NotFound />
  }
  // The key gives every match fresh state, and the old match closes its stream on unmount.
  return <MatchView key={matchId} matchId={matchId} />
}

function NotFound() {
  return (
    <StateMessage icon="🔍" title="Match not found" text="This match does not exist.">
      <Link to="/" className="button">
        Back to matches
      </Link>
    </StateMessage>
  )
}

function MatchView({ matchId }: { matchId: number }) {
  const matches = useFetch(fetchMatches, [])
  const { score, connection } = useScoreStream(matchId)
  const [searchParams, setSearchParams] = useSearchParams()
  const [starting, setStarting] = useState(false)
  const [startError, setStartError] = useState<string | null>(null)
  const tab = parseTab(searchParams.get('tab'))

  if (matches.loading) return <SkeletonPanel />
  if (matches.error || !matches.data) {
    return <ErrorState message={matches.error ?? 'No data'} onRetry={matches.retry} />
  }
  const match = matches.data.find((item) => item.id === matchId)
  if (!match) return <NotFound />

  // The stream knows the newest status, the match list was loaded once on arrival.
  const status = score?.status ?? match.status
  const colors = {
    '--team-a': teamColor(match.teamA.shortName),
    '--team-b': teamColor(match.teamB.shortName),
  } as CSSProperties

  // No need to use the response: the server pushes the new score through the stream.
  async function handleStart() {
    setStarting(true)
    setStartError(null)
    try {
      await startMatch(matchId)
    } catch (error) {
      setStartError(errorMessage(error))
    } finally {
      setStarting(false)
    }
  }

  return (
    <>
      <Link to="/" className="back-link">
        ← All matches
      </Link>
      <header className="match-header card" style={colors}>
        <div className="match-header-main">
          <div className="match-header-teams">
            <TeamMark shortName={match.teamA.shortName} />
            <h1>
              {match.teamA.name} <span className="versus">vs</span> {match.teamB.name}
            </h1>
            <TeamMark shortName={match.teamB.shortName} />
          </div>
          <p className="muted">
            {match.venue} · {match.totalOvers} overs
          </p>
        </div>
        <div className="match-header-side">
          <StatusBadge status={status} />
          {status === 'SCHEDULED' && (
            <button type="button" className="button primary" onClick={handleStart} disabled={starting}>
              {starting ? 'Starting…' : 'Start match'}
            </button>
          )}
        </div>
        {startError && <p className="error match-header-error">{startError}</p>}
      </header>

      <Tabs tabs={TABS} active={tab} onChange={(id) => setSearchParams({ tab: id }, { replace: true })} />

      {tab === 'live' && <LivePanel matchId={matchId} score={score} connection={connection} />}
      {tab === 'scorecard' && <ScorecardPanel matchId={matchId} status={status} />}
      {tab === 'summary' && <SummaryPanel matchId={matchId} status={status} />}
    </>
  )
}

export default MatchPage
