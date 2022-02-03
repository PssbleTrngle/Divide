import { memo, VFC } from 'react'
import styled from 'styled-components'
import { Inline } from '../Text'
import { Data, SeriesContext } from './types'
import { formatTime } from './util'

const DataTooltip: VFC<
   Data & {
      series: SeriesContext
      label?: string
   }
> = ({ series, label, info, ...props }) => {
   const time = props.type === 'blob' ? props.from.time : props.time
   const value = props.type === 'blob' ? `${props.from.value} - ${props.to.value}` : props.value

   return (
      <Style>
         <span>{label ?? formatTime(time)}</span>
         <Inline center>{series.label}</Inline>
         <span>
            {value} {series.unit}
         </span>
         {info}
      </Style>
   )
}

const Style = styled.div`
   display: grid;
   justify-content: center;
   row-gap: 0.5em;
`

export default memo(DataTooltip)
