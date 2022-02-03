import { groupBy, orderBy } from 'lodash'
import { adjustHue } from 'polished'
import { PropsWithChildren, ReactNode, useEffect, useMemo, useState } from 'react'
import { Chart } from '../../hooks/useChart'
import useGameData from '../../hooks/useGameData'
import { Event, EventType, EventTypes } from '../../models/events'
import { exists } from '../../util'
import SelectionBar from '../SelectionBar'
import Legend from './Legend'
import LineChart from './LineChart'
import { DataPoint } from './types'

function EventChart<T extends EventType, O extends { id: string; color?: string }>({
   value,
   group,
   ownerOf,
   label,
   info,
   type,
   unit = '',
   children,
}: PropsWithChildren<{
   type: T
   value: (event: EventTypes[T], i: number) => DataPoint['value']
   group?: (event: EventTypes[T]) => unknown
   ownerOf: (event: EventTypes[T]) => O | undefined
   label: (owner: O, color?: string) => ReactNode
   info?: (event: EventTypes[T]) => ReactNode
   unit?: string
}>) {
   const { data } = useGameData<Event<T>[]>(`events/${type}`)

   const grouped = useMemo(() => {
      return Object.entries(groupBy(data ?? [], e => group?.(e.event) ?? '')).map(([groupLabel, events]) => ({
         label: groupLabel,
         data: orderBy(
            Object.entries(groupBy(events ?? [], e => ownerOf(e.event)?.id)).map(([, events]) => ({
               events,
               owner: ownerOf(events[0].event),
            })),
            s => s.owner?.id
         )
            .map(({ owner, events }, i, a) => {
               if (!owner) return null
               const color = owner.color ?? adjustHue((360 / a.length) * i, '#dd2e2e')
               return {
                  label: label(owner, color),
                  id: owner.id,
                  color,
                  unit,
                  data: events.map((e, i) => ({
                     value: value(e.event, i + 1),
                     time: e.realTime,
                     id: e.id,
                     info: info?.(e.event),
                  })),
               }
            })
            .filter(exists),
      }))
   }, [data, group, ownerOf, unit, value, label, info])

   const [selected, setSelected] = useState<string>()
   useEffect(() => {
      if (data)
         setSelected(current => {
            if (grouped.some(s => s.label === current)) return current
            return grouped[0]?.label
         })
   }, [data, grouped])

   const series = useMemo(() => grouped.find(s => s.label === selected), [grouped, selected])

   return (
      <>
         {grouped.length > 1 && (
            <SelectionBar values={grouped.map(g => g.label)} value={selected} onChange={setSelected} />
         )}
         {series && (
            <Chart initial={0} series={series.data}>
               <LineChart />
               <Legend />
               {children}
            </Chart>
         )}
      </>
   )
}

export default EventChart
