import { groupBy, uniq } from 'lodash'
import { useEffect, useMemo, useState } from 'react'
import useApi from '../../hooks/useApi'
import { Team } from '../../hooks/useSession'
import { colorOf } from '../../util'
import { Event, EventType, EventTypes } from '../events/types'
import SelectionBar from '../SelectionBar'
import LineChart from './LineChart'
import { DataPoint } from './types'

function EventChart<T extends EventType>({
   value,
   group,
   teamOf,
   type,
   unit = type,
}: {
   type: T
   value: (event: EventTypes[T]) => DataPoint['value']
   group: (event: EventTypes[T]) => unknown
   teamOf: (event: EventTypes[T]) => Team
   unit?: string
}) {
   const { data } = useApi<Event<T>[]>(`events/${type}`)
   //const { data: teams } = useApi<Team[]>('team')

   const teams = useMemo(() => uniq(data?.map(it => teamOf(it.event))), [data])

   const grouped = useMemo(() => {
      const grouped = Object.entries(groupBy(data ?? [], e => group(e.event))).map(([label, events]) => ({
         label,
         events,
      }))
      return grouped.map(({ events, label }) => ({
         label,
         series: Object.entries(groupBy(events ?? [], e => teamOf(e.event).id))
            .map(([team, events]) => ({
               events,
               team: teams.find(it => it.id === team),
            }))
            .filter(it => !!it.team),
      }))
   }, [data, group, teams, teamOf])

   const [selected, setSelected] = useState<string>()
   useEffect(() => {
      if (data)
         setSelected(current => {
            if (grouped.some(s => s.label === current)) return current
            return grouped[0]?.label
         })
   }, [data, teams])

   const series = useMemo(() => grouped.find(s => s.label === selected), [grouped, selected])

   return (
      <>
         {grouped.length > 1 && (
            <SelectionBar values={grouped.map(g => g.label)} value={selected} onChange={setSelected} />
         )}
         {series && (
            <LineChart
               key={series.label}
               initial={0}
               series={series.series.map(({ team, events }) => ({
                  label: `${team?.name}`,
                  unit,
                  color: colorOf(team?.color),
                  data: events.map(e => ({ value: value(e.event), time: e.realTime, id: e.id })),
               }))}
            />
         )}
      </>
   )
}

export default EventChart
