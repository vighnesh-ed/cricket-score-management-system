import type { BatsmanScore, PlayerSummary } from '../types.ts'

interface CurrentPlayersProps {
  striker: BatsmanScore | null
  nonStriker: BatsmanScore | null
  bowler: PlayerSummary | null
}

function CurrentPlayers({ striker, nonStriker, bowler }: CurrentPlayersProps) {
  return (
    <div className="players">
      <div>
        <h3 className="panel-title">Batting</h3>
        <ul className="player-list">
          {striker && <BatsmanRow batsman={striker} onStrike />}
          {nonStriker && <BatsmanRow batsman={nonStriker} onStrike={false} />}
        </ul>
      </div>
      <div>
        <h3 className="panel-title">Bowling</h3>
        <p className="player-row">{bowler ? bowler.name : '–'}</p>
      </div>
    </div>
  )
}

interface BatsmanRowProps {
  batsman: BatsmanScore
  onStrike: boolean
}

function BatsmanRow({ batsman, onStrike }: BatsmanRowProps) {
  return (
    <li className={onStrike ? 'player-row on-strike' : 'player-row'}>
      <span>
        {batsman.name}
        {onStrike && (
          <span className="strike-mark" title="On strike" aria-label="on strike">
            {' '}
            *
          </span>
        )}
      </span>
      <span className="player-figures">
        {batsman.runs} ({batsman.balls})
      </span>
    </li>
  )
}

export default CurrentPlayers
