import { maxBy, minBy, orderBy } from 'lodash'
import { createContext, Dispatch, FC, SetStateAction, useContext, useMemo, useState } from 'react'
import ReactTooltip from 'react-tooltip'
import styled from 'styled-components'
import DataTooltip from '../components/charts/DataTooltip'
import { Data, DataProps, Series } from '../components/charts/types'
import useTooltip from './useTooltip'

export interface ChartContext {
   series: Series[]
   setHidden: Dispatch<SetStateAction<string[]>>
   hover: (data?: Data & DataProps) => void
   initial?: number
   min: number
   max: number
   start: number
   end: number
}

const CTX = createContext<ChartContext | null>(null)

export default function useChartContext() {
   const ctx = useContext(CTX)
   if (ctx) return ctx
   throw new Error('Not inside a chart element')
}

export const Chart: FC<{ series: Series[]; initial?: number }> = ({ children, initial, ...props }) => {
   const [hidden, setHidden] = useState<string[]>([])

   const series = useMemo(
      () =>
         orderBy(
            props.series.map(s => ({ ...s, hidden: hidden.includes(s.id) })),
            s => s.id
         ),
      [props.series, hidden]
   )
   const points = useMemo(() => series.map(s => s.data).flat(), [series])

   const bounds = useMemo(() => {
      const min = Math.min(initial ?? Number.MAX_SAFE_INTEGER, minBy(points, p => p.value)?.value ?? 0)
      const max = Math.max(initial ?? Number.MIN_SAFE_INTEGER, maxBy(points, p => p.value)?.value ?? 0)

      const start = minBy(points, p => p.time)?.time ?? 0
      const end = maxBy(points, p => p.time)?.time ?? 0
      return { min, max, start, end }
   }, [points, initial])

   const [hovered, hover] = useState<Data & DataProps>()
   const context = useMemo<ChartContext>(
      () => ({ ...bounds, series, initial, hover, setHidden }),
      [bounds, series, hover, initial, setHidden]
   )

   useTooltip()

   return (
      <ChartProvider value={context}>
         <Style>{children}</Style>
         <ReactTooltip effect='solid' id='chart' getContent={() => hovered && <DataTooltip {...hovered} />} />
      </ChartProvider>
   )
}

const Style = styled.div`
   display: grid;
   grid-template: 'graph legend';
   justify-content: center;
   gap: 0.5em;
`

export const ChartProvider = CTX.Provider
