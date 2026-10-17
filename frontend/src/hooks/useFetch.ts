import { useEffect, useState } from 'react'
import { errorMessage } from '../api.ts'

interface FetchState<T> {
  data: T | null
  error: string | null
  loading: boolean
}

// Loads data when `deps` change and, if `refreshMs` is set, again on an interval.
// Refreshes are silent: old data stays on screen and a failed refresh is ignored.
// `fetcher` is left out of the effect dependencies on purpose, `deps` say when it changes.
export function useFetch<T>(fetcher: () => Promise<T>, deps: readonly unknown[], refreshMs?: number) {
  const [state, setState] = useState<FetchState<T>>({ data: null, error: null, loading: true })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let cancelled = false
    const load = () => {
      fetcher()
        .then((data) => {
          if (!cancelled) setState({ data, error: null, loading: false })
        })
        .catch((error: unknown) => {
          if (!cancelled) {
            setState((current) =>
              current.data ? current : { data: null, error: errorMessage(error), loading: false },
            )
          }
        })
    }
    load()
    const timer = refreshMs ? setInterval(load, refreshMs) : undefined
    return () => {
      cancelled = true
      clearInterval(timer)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, refreshMs, attempt])

  function retry() {
    setState({ data: null, error: null, loading: true })
    setAttempt((current) => current + 1)
  }

  return { ...state, retry }
}
