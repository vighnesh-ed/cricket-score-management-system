interface RecentBallsProps {
  balls: string[]
}

// The backend labels balls as runs ("0".."6"), "W" for a wicket and "Wd" / "Nb" for extras.
function chipClass(ball: string): string {
  switch (ball) {
    case 'W':
      return 'chip chip-wicket'
    case '4':
      return 'chip chip-four'
    case '6':
      return 'chip chip-six'
    case 'Wd':
    case 'Nb':
      return 'chip chip-extra'
    default:
      return 'chip'
  }
}

function RecentBalls({ balls }: RecentBallsProps) {
  return (
    <div>
      <h3 className="panel-title">Last 6 balls</h3>
      {balls.length === 0 ? (
        <p className="muted">No balls bowled yet.</p>
      ) : (
        <ol className="chips">
          {balls.map((ball, index) => (
            <li key={index} className={chipClass(ball)}>
              {ball}
            </li>
          ))}
        </ol>
      )}
    </div>
  )
}

export default RecentBalls
