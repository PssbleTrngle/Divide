import { groupBy, max, min, orderBy } from 'lodash'
import { DateTime } from 'luxon'
import { darken } from 'polished'
import { Fragment, useMemo, VFC } from 'react'
import styled from 'styled-components'
import useChartContext from '../../hooks/useChart'
import { exists } from '../../util'
import Blob from './Blob'
import Line from './Line'
import Point from './Point'
import { Data, DataPoint, Series } from './types'
import { pointsOf } from './util'
import XAxis from './XAxis'
import YAxis from './YAxis'

interface Props {
   normalize?: boolean
   blobify?: boolean
}

const LineChart: VFC<Props> = props => {
   const { series } = useChartContext()

   return (
      <Style>
         <YAxis />
         <XAxis />
         <SVG>
            {series.map(series => (
               <SeriesPoints key={series.id} {...props} {...series}></SeriesPoints>
            ))}
         </SVG>
      </Style>
   )
}

const SeriesPoints: VFC<Series & Props> = ({ data, hidden, normalize, blobify = true, ...ctx }) => {
   const { initial, start } = useChartContext()

   const startingPoint = useMemo(
      () => (typeof initial === 'number' ? { id: '', time: start, value: initial, x: 0, label: 'Start' } : undefined),
      [initial, start]
   )

   const sorted = useMemo<DataPoint[]>(
      () =>
         [startingPoint, ...orderBy(data, p => p.time)]
            .filter(exists)
            .map((p, i, a) => (normalize ? { ...p, x: p.x ?? i / a.length } : p)),
      [data, startingPoint, normalize]
   )

   const blobs = useMemo(() => {
      const grouped = groupBy(sorted, s => DateTime.fromMillis(s.time).toFormat('hh-mm'))
      return Object.values(grouped).map<Data>(points => {
         const first = points[0]
         if (!blobify || points.length === 1) return { ...first, type: 'point' }

         const keys = Object.keys(first) as Array<keyof DataPoint>
         const [from, to] = [min, max].map(fn =>
            keys.reduce((o, k) => ({ ...o, [k]: fn(points.map(p => p[k])) }), {} as DataPoint)
         )

         return {
            type: 'blob',
            id: first.id,
            x: from.x,
            values: points.map(p => p.value),
            from,
            to,
         }
      })
   }, [sorted, blobify])

   const withNext = useMemo(() => blobs.map((points, i) => [points, blobs[i - 1]]), [blobs])

   if (hidden) return null

   return (
      <>
         {withNext.map(([data, previous]) => (
            <Fragment key={data.id}>
               {data.type === 'point' && <Point series={ctx} {...data} />}
               {data.type === 'blob' && <Blob series={ctx} {...data} />}
               {previous && <Line {...ctx} from={pointsOf(previous).to} to={pointsOf(data).from} />}
            </Fragment>
         ))}
      </>
   )
}

const Style = styled.section`
   grid-area: graph;

   margin-bottom: 5em;

   display: grid;
   gap: 0.5em;
   grid-template:
      'y graph'
      '. x';
`

const SVG = styled.svg`
   border-radius: 0.5em;
   grid-area: graph;
   margin: 0 auto;
   height: 500px;
   min-width: 800px;
   background: ${p => darken(0.05, p.theme.bg)};
`

export default LineChart
