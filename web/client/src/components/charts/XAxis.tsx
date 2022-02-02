import { DateTime } from 'luxon'
import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import useChartContext from '../../hooks/useChart'

const XAxis: VFC<{ precicion?: number }> = ({ precicion = 10 }) => {
   const { start, end } = useChartContext()
   const values = useMemo(() => {
      const [min, max] = [start, end].map(it => DateTime.fromMillis(it))
      const minutes = max.diff(min).as('minutes')
      return new Array(precicion + 1).fill(null).map((_, i) => min.plus({ minutes: (i / precicion) * minutes }))
   }, [start, end, precicion])

   return (
      <Style>
         {values.map(v => (
            <span key={v.toMillis()}>{v.toLocaleString(DateTime.TIME_SIMPLE)}</span>
         ))}
      </Style>
   )
}

const Style = styled.div`
   grid-area: x;
   display: grid;
   align-items: center;
   justify-content: space-between;
   grid-auto-flow: column;
`

export default XAxis
