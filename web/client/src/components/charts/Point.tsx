import { darken } from 'polished'
import { memo, VFC } from 'react'
import { animated, useSpring } from 'react-spring'
import { useTheme } from 'styled-components'
import { colorOf } from '../../util'
import DataWrapper from './DataWrapper'
import { DataPoint, DataProps } from './types'
import { usePointPos } from './util'

const Point: VFC<DataPoint & DataProps> = props => {
   return <DataWrapper type='point' {...props}>{p => <Render {...props} {...p} />}</DataWrapper>
}

const Render: VFC<DataPoint & DataProps & { hovered?: boolean }> = ({ time, series, value, x, y, hovered }) => {
   const { radius } = useSpring({ radius: hovered ? 1 : 0.5 })
   const pos = usePointPos({ time, value, x, y })
   const { bg } = useTheme()
   return (
      <>
         <circle r='1.5%' color={darken(0.05, bg)} opacity={0.001} cx={`${pos.x}%`} cy={`${pos.y}%`} />
         <animated.circle
            r={radius.to(r => `${r}%`)}
            strokeWidth={radius.to(r => `${1.6 - r}%`)}
            cx={`${pos.x}%`}
            cy={`${pos.y}%`}
            color={colorOf(series.color)}
         />
      </>
   )
}

export default memo(Point)
