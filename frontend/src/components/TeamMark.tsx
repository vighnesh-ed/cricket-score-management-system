import { teamStyle } from '../teams.ts'

interface TeamMarkProps {
  shortName: string
}

function TeamMark({ shortName }: TeamMarkProps) {
  return (
    <span className="team-mark" style={teamStyle(shortName)} aria-hidden="true">
      {shortName}
    </span>
  )
}

export default TeamMark
