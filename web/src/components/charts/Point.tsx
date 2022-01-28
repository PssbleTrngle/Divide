import { DateTime } from 'luxon'
import { memo, useState, VFC } from 'react'
import { animated, useSpring } from 'react-spring'
import { DataPoint, SeriesContext } from './types'

export function relativePos(min: number, max: number, value: number, invert = false, padding = 5) {
   const percentage = ((value - min) / (max - min)) * (100 - padding) + padding / 2
   return invert ? `${100 - percentage}%` : `${percentage}%`
}

const Point: VFC<SeriesContext & DataPoint> = ({ unit, time, value, min, max, start, end, color = 'white', label, ...pos }) => {
   const [hovered, setHovered] = useState(false)
   const { radius } = useSpring({ radius: hovered ? 0.5 : 0.25 })
   return (
      <animated.circle
         r={radius.to(r => `${r}em`)}
         data-tip={
            label ?? `${DateTime.fromMillis(time).toLocaleString(DateTime.TIME_WITH_SECONDS)} : ${value} ${unit ?? ''}`
         }
         onMouseEnter={() => setHovered(true)}
         onMouseLeave={() => setHovered(false)}
         cx={pos.x ? `${pos.x * 100}%` : relativePos(start, end, time)}
         cy={pos.y ? `${pos.y * 100}%` : relativePos(min, max, value, true)}
         color={color}
      />
   )
}

export default memo(Point)
