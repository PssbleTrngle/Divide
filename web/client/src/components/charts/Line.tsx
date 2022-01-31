import { memo, VFC } from 'react'
import { pointsPos } from './Point'
import { DataPoint, SeriesContext } from './types'

const Line: VFC<
   SeriesContext & {
      from: DataPoint
      to: DataPoint
   }
> = ({ from, to, color = 'white', ...ctx }) => {
   const fromPos = pointsPos(from, ctx)
   const toPos = pointsPos(to, ctx)
   return <line x1={fromPos.x} y1={fromPos.y} x2={toPos.x} y2={toPos.y} stroke={color} />
}

export default memo(Line)
