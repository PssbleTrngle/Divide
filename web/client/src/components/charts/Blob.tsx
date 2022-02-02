import { memo, VFC } from 'react'
import { animated, useSpring } from 'react-spring'
import { colorOf } from '../../util'
import DataWrapper from './DataWrapper'
import { DataBlob, DataProps } from './types'
import { usePointPos } from './util'

const Blob: VFC<DataBlob & DataProps> = props => {
   return (
      <DataWrapper type='blob' {...props}>
         {p => <Render {...props} {...p} />}
      </DataWrapper>
   )
}

const Render: VFC<
   DataBlob &
      DataProps & {
         hovered?: boolean
      }
> = ({ series, from, to, hovered }) => {
   const { radius } = useSpring({ radius: hovered ? 1 : 0.5 })

   const minPos = usePointPos(from)
   const maxPos = usePointPos(to)

   return (
      <animated.rect
         strokeWidth={radius.to(r => `${r}%`)}
         x={`${minPos.x}%`}
         y={`${maxPos.y}%`}
         width='0.5%'
         height={`${Math.max(0.5, minPos.y - maxPos.y)}%`}
         fill={colorOf(series.color)}
         ry='0.5%'
         rx='0.5%'
         stroke={colorOf(series.color)}
      />
   )
}

export default memo(Blob)
