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
   const sorted = useMemo(() => orderBy(data, p => p.time).filter(exists), [data, initial])
   const [first, ...rest] = sorted
   const withNext = useMemo(
      () => rest.map((point, i) => [point, sorted[i]].map((p, ii) => ({ ...p, x: (i - ii) / sorted.length }))),
      [sorted]
   )

   return (
      <>
         {typeof initial === 'number' && (
            <>
               <Point {...values} label='Start' x={0} time={values.start} value={initial} />
               <Line {...values} from={{ time: values.start - 10, value: initial }} to={first} />
            </>
         )}
         {withNext.map(([point, previous], i) => (
            <Fragment key={point.id}>
               <Point {...values} {...point} />
               {previous && <Point {...values} {...previous} />}
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
