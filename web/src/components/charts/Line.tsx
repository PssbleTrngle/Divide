import { memo, VFC } from 'react'
import { relativePos } from './Point'
import { DataPoint, SeriesContext } from './types'

const Line: VFC<
   SeriesContext & {
      from: DataPoint
      to: DataPoint
   }
> = ({ from, to, min, max, start, end, color = 'white' }) => (
   <line
      x1={relativePos(start, end, from.time)}
      y1={relativePos(min, max, from.value, true)}
      x2={relativePos(start, end, to.time)}
      y2={relativePos(min, max, to.value, true)}
      stroke={color}
   />
)

export default memo(Line)
