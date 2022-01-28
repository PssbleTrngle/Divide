import { maxBy, minBy, orderBy } from 'lodash'
import { darken } from 'polished'
import { Fragment, useMemo, VFC } from 'react'
import styled from 'styled-components'
import useTooltip from '../../hooks/useTooltip'
import { exists } from '../../util'
import Line from './Line'
import Point from './Point'
import { DataPoint, SeriesContext } from './types'

interface Series {
   data: (DataPoint & { id: string })[]
   label?: string
   color?: string
   unit?: string
}

const LineChart: VFC<{
   series: Series[]
   initial?: number
}> = ({ series, initial }) => {
   const points = useMemo(() => series.map(s => s.data).flat(), [series])

   const values = useMemo(() => {
      const min = Math.min(initial ?? Number.MAX_SAFE_INTEGER, minBy(points, p => p.value)?.value ?? 0)
      const max = Math.max(initial ?? Number.MIN_SAFE_INTEGER, maxBy(points, p => p.value)?.value ?? 0)

      const start = minBy(points, p => p.time)?.time ?? 0
      const end = maxBy(points, p => p.time)?.time ?? 0
      return { min, max, start, end, initial }
   }, [points, initial])

   useTooltip()

   return (
      <Style>
         {series.map(series => (
            <Series key={series.label} {...values} {...series}></Series>
         ))}
      </Style>
   )
}

const Series: VFC<Series & SeriesContext & { initial?: number }> = ({ data, label, initial, ...values }) => {
   const startingPoint = useMemo(
      () =>
         typeof initial === 'number'
            ? { id: '', time: values.start - 10, value: initial, x: 0, label: 'Start' }
            : undefined,
      [initial]
   )

   const sorted = useMemo(
      () => [startingPoint, ...orderBy(data, p => p.time)].filter(exists).map((p, i, a) => ({ ...p, x: i / a.length })),
      [data, startingPoint]
   )

   const withNext = useMemo(() => sorted.map((point, i) => [point, sorted[i - 1]]), [sorted])

   return (
      <>
         {withNext.map(([point, previous]) => (
            <Fragment key={point.id}>
               <Point {...values} {...point} />
               {previous && <Line {...values} from={point} to={previous} />}
            </Fragment>
         ))}
      </>
   )
}

const Style = styled.svg`
   height: 500px;
   min-width: 800px;
   background: ${p => darken(0.05, p.theme.bg)};
`

export default LineChart
