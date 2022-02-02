import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import useChartContext from '../../hooks/useChart'

const YAxis: VFC<{ precicion?: number }> = ({ precicion = 10 }) => {
   const { min, max } = useChartContext()
   const values = useMemo(() => {
      const diff = max - min
      return new Array(precicion + 1)
         .fill(null)
         .map((_, i) => (i / precicion) * diff + min)
         .reverse()
   }, [min, max, precicion])

   return (
      <Style>
         {values.map(v => (
            <span key={v}>{v.toFixed()}</span>
         ))}
      </Style>
   )
}

const Style = styled.div`
   grid-area: y;
   display: grid;
   align-items: center;
   justify-content: space-between;
   padding: 0 0.5em;
`

export default YAxis
