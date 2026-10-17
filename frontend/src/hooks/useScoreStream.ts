import { useEffect, useState } from 'react'
import { openScoreStream } from '../api.ts'
import type { ScoreResponse } from '../types.ts'

export type ConnectionState = 'connecting' | 'live' | 'reconnecting'

// The EventSource retries on its own after an error, so "reconnecting" turns back into "live" by itself.
export function useScoreStream(matchId: number) {
  const [score, setScore] = useState<ScoreResponse | null>(null)
  const [connection, setConnection] = useState<ConnectionState>('connecting')

  useEffect(() => {
    const source = openScoreStream(matchId, {
      onScore: setScore,
      onOpen: () => setConnection('live'),
      onError: () => setConnection('reconnecting'),
    })
    return () => source.close()
  }, [matchId])

  return { score, connection }
}
