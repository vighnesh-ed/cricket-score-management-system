import { fetchSummary } from '../api.ts'
import { useFetch } from '../hooks/useFetch.ts'
import type { ConnectionState } from '../hooks/useScoreStream.ts'
import { teamColor } from '../teams.ts'
import type { ScoreResponse } from '../types.ts'
import BarChart from './BarChart.tsx'
import ConnectionIndicator from './ConnectionIndicator.tsx'
import CurrentPlayers from './CurrentPlayers.tsx'
import RecentBalls from './RecentBalls.tsx'
import ScoreSummary from './ScoreSummary.tsx'
import { SkeletonPanel } from './Skeleton.tsx'
import StateMessage from './StateMessage.tsx'

interface LivePanelProps {
  matchId: number
  score: ScoreResponse | null
  connection: ConnectionState
}

function LivePanel({ matchId, score, connection }: LivePanelProps) {
  // The stream sends a new score after every ball, so the chart is reloaded whenever the score changes.
  const summary = useFetch(
    () => fetchSummary(matchId),
    [matchId, score?.runs, score?.wickets, score?.overs, score?.inningsNumber],
  )

  if (!score) {
    return <SkeletonPanel />
  }
  if (!score.battingTeam) {
    return (
      <StateMessage
        icon="🏏"
        title="This match has not started yet"
        text="Press Start match above to begin the simulation."
      />
    )
  }

  const innings = summary.data?.innings.find((item) => item.inningsNumber === score.inningsNumber)

  return (
    <div className="stack">
      {score.status === 'COMPLETED' && <p className="result-banner">{score.result}</p>}
      <article className="card scoreboard">
        <div className="scoreboard-top">
          <ScoreSummary score={score} team={score.battingTeam} />
          <ConnectionIndicator state={connection} />
        </div>
        <RecentBalls balls={score.lastSixBalls} />
      </article>
      <div className="two-columns">
        <article className="card">
          <CurrentPlayers striker={score.striker} nonStriker={score.nonStriker} bowler={score.bowler} />
        </article>
        <article className="card">
          {innings ? (
            <BarChart
              title={`${score.battingTeam.name} runs per over`}
              values={innings.runsPerOver}
              color={teamColor(score.battingTeam.shortName)}
            />
          ) : (
            <p className="muted">Chart appears after the first over.</p>
          )}
        </article>
      </div>
    </div>
  )
}

export default LivePanel
