import { DateTime } from 'luxon'
import { darken } from 'polished'
import { memo, useState, VFC } from 'react'
import { animated, useSpring } from 'react-spring'
import { useTheme } from 'styled-components'
import { DataPoint, SeriesContext } from './types'

function pad(percentage: number, padding = 0.05) {
   return percentage * (1 - padding * 2) + padding * 100
}

export function pointsPos({ time, value, ...pos }: DataPoint, { min, max, start, end }: SeriesContext) {
   const x = `${pad(pos.x ? pos.x * 100 : relativePos(start, end, time))}%`
   const y = `${pad(pos.y ? pos.y * 100 : relativePos(min, max, value, true))}%`
   return { x, y }
}

function relativePos(min: number, max: number, value: number, invert = false) {
   const percentage = ((value - min) / (max - min)) * 100
   return invert ? 100 - percentage : percentage
}

const Point: VFC<SeriesContext & DataPoint> = ({ unit, time, value, color = 'white', label, x, y, ...ctx }) => {
   const [hovered, setHovered] = useState(false)
   const { radius } = useSpring({ radius: hovered ? 0.8 : 0.4 })
   const pos = pointsPos({ time, value, x, y }, ctx)
   const { bg } = useTheme()

   return (
      <g onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)}>
         <circle r='1.2em' color={darken(0.05, bg)} opacity={0.001} cy={pos.y} cx={pos.x} />
         <animated.circle
            r={radius.to(r => `${r}em`)}
            data-tip={
               label ??
               `${DateTime.fromMillis(time).toLocaleString(DateTime.TIME_WITH_SECONDS)} : ${value} ${unit ?? ''}`
            }
            strokeWidth={radius.to(r => `${1.6 - r}em`)}
            cx={pos.x}
            cy={pos.y}
            color={color}
         />
      </g>
   )
}

export default memo(Point)
