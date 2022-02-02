import { memo, VFC } from 'react'
import styled from 'styled-components'
import { Data, SeriesContext } from './types'
import { formatTime } from './util'

const DataTooltip: VFC<
   Data & {
      series: SeriesContext
      label?: string
   }
> = ({ series, label, ...props }) => {
   const time = props.type === 'blob' ? props.from.time : props.time
   const value = props.type === 'blob' ? `${props.from.value} - ${props.to.value}` : props.value

   return (
      <Style>
         <span>{label ?? formatTime(time)}</span>
         <Label>{series.label}</Label>
         <span>
            {value} {series.unit}
         </span>
      </Style>
   )
}

const Label = styled.span`
   display: grid;
   grid-auto-flow: column;
   gap: 0.5em;
`

const Style = styled.div`
   display: grid;
`

export default memo(DataTooltip)
