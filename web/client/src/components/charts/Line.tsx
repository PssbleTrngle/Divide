import { memo, VFC } from 'react'
import styled from 'styled-components'
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
      <Style x1={`${fromPos.x}%`} y1={`${fromPos.y}%`} x2={`${toPos.x}%`} y2={`${toPos.y}%`} stroke={colorOf(color)} />
   )
}

const Style = styled.line`
   pointer-events: none;
   opacity: 0.5;
`

export default memo(Line)
