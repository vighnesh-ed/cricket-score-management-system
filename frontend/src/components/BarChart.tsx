interface BarChartProps {
  title: string
  values: number[]
  color: string
}

const WIDTH = 400
const HEIGHT = 190
const PADDING = { top: 18, right: 8, bottom: 24, left: 26 }
const GRID_LINES = 4

// Runs per over drawn with plain SVG. Over numbers start at 1 for readers.
function BarChart({ title, values, color }: BarChartProps) {
  const plotWidth = WIDTH - PADDING.left - PADDING.right
  const plotHeight = HEIGHT - PADDING.top - PADDING.bottom
  const gridStep = Math.ceil(Math.max(5, ...values) / GRID_LINES)
  const slot = plotWidth / Math.max(values.length, 1)
  const barWidth = Math.min(slot * 0.68, 22)
  const labelEvery = values.length > 12 ? 5 : 1

  const y = (value: number) => PADDING.top + plotHeight - (value / (gridStep * GRID_LINES)) * plotHeight

  return (
    <figure className="chart">
      <figcaption>{title}</figcaption>
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label={`${title}: ${values.join(', ')}`}>
        {Array.from({ length: GRID_LINES + 1 }, (_, index) => {
          const value = index * gridStep
          return (
            <g key={value}>
              <line
                className="chart-grid"
                x1={PADDING.left}
                x2={WIDTH - PADDING.right}
                y1={y(value)}
                y2={y(value)}
              />
              <text className="chart-label" x={PADDING.left - 5} y={y(value) + 3} textAnchor="end">
                {value}
              </text>
            </g>
          )
        })}
        {values.map((value, index) => {
          const x = PADDING.left + slot * index + (slot - barWidth) / 2
          return (
            <g key={index} className="chart-bar">
              <title>{`Over ${index + 1}: ${value} runs`}</title>
              <rect
                x={x}
                y={y(value)}
                width={barWidth}
                height={Math.max(y(0) - y(value), 1)}
                rx={3}
                fill={color}
              />
              {values.length <= 12 && (
                <text className="chart-value" x={x + barWidth / 2} y={y(value) - 4} textAnchor="middle">
                  {value}
                </text>
              )}
              {(index + 1) % labelEvery === 0 && (
                <text className="chart-label" x={x + barWidth / 2} y={HEIGHT - 8} textAnchor="middle">
                  {index + 1}
                </text>
              )}
            </g>
          )
        })}
      </svg>
      <p className="chart-axis muted">Overs</p>
    </figure>
  )
}

export default BarChart
