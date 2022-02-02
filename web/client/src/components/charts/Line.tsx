import { memo, VFC } from 'react'
import { colorOf } from '../../util'
import { DataPoint, SeriesContext } from './types'
import { usePointPos } from './util'

const Line: VFC<{
   from: DataPoint
   to: DataPoint
   color?: SeriesContext['color']
}> = ({ from, to, color = 'white' }) => {
   const fromPos = usePointPos(from)
   const toPos = usePointPos(to)
   return (
      <line
         opacity={0.5}
         x1={`${fromPos.x}%`}
         y1={`${fromPos.y}%`}
         x2={`${toPos.x}%`}
         y2={`${toPos.y}%`}
         stroke={colorOf(color)}
      />
   )
}

export default memo(Line)
