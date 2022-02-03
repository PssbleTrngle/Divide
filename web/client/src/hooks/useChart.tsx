import { maxBy, minBy, orderBy } from 'lodash'
import { darken } from 'polished'
import { createContext, Dispatch, FC, SetStateAction, useCallback, useContext, useMemo, useState } from 'react'
import ReactTooltip from 'react-tooltip'
import styled from 'styled-components'
import Box from '../components/Box'
import DataTooltip from '../components/charts/DataTooltip'
import { Data, DataProps, Series } from '../components/charts/types'
import useTooltip from './useTooltip'

export interface ChartContext {
   series: Series[]
   setHidden: Dispatch<SetStateAction<string[]>>
   hover: (data: Data & DataProps) => void
   blur: (data?: Data & DataProps) => void
   initial?: number
   min: number
   max: number
   start: number
   end: number
   id: string
}

const CTX = createContext<ChartContext | null>(null)

export function useInChart() {
   return !!useContext(CTX)
}

export default function useChartContext() {
   const ctx = useContext(CTX)
   if (ctx) return ctx
   throw new Error('Not inside a chart element')
}

export const Chart: FC<{ series: Series[]; initial?: number }> = ({ children, initial, ...props }) => {
   const [hidden, setHidden] = useState<string[]>([])
   const id = useMemo(() => (Math.random() * 100000).toFixed(), [])

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
   const blur = useCallback(
      (d?: Data & DataProps) => {
         hover(c => {
            if (d && c && d.id !== c.id) return c
            return undefined
         })
      },
      [hover]
   )

   const context = useMemo<ChartContext>(
      () => ({ ...bounds, series, initial, hover, setHidden, id, blur }),
      [bounds, series, hover, initial, setHidden, id, blur]
   )

   useTooltip()

   return (
      <ChartProvider value={context}>
         <Style>{children}</Style>
         <ReactTooltip effect='solid' id={`chart-${id}`} getContent={() => hovered && <DataTooltip {...hovered} />} />
      </ChartProvider>
   )
}

const Style = styled(Box)`
   background: ${p => darken(0.02, p.theme.bg)};
   grid-template: 'graph legend';
   align-items: start;
`

export const ChartProvider = CTX.Provider
