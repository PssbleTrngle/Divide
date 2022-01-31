import { groupBy, orderBy } from 'lodash'
import { darken } from 'polished'
import { useMemo, useState, VFC } from 'react'
import { useLocation } from 'react-router-dom'
import styled from 'styled-components'
import EventLine from '../../components/events/EventLine'
import Timestamp from '../../components/events/Timestamp'
import Input from '../../components/Input'
import Page from '../../components/Page'
import useGameData from '../../hooks/useGameData'
import { EXCLUDED_EVENTS } from '../../hooks/useSocket'
import { Event } from '../../models/events'

function searchRecursive<T>(value: T, term: string): boolean {
   return Object.values(value).some(v => {
      if (typeof v === 'string') return v.toLowerCase().includes(term)
      if (typeof v === 'number') return v.toString().includes(term)
      if (typeof v === 'object') return searchRecursive(v, term)
      return false
   })
}

const Events: VFC = () => {
   const { data } = useGameData<Event[]>('events', { refetchInterval: false })

   const location = useLocation()
   const max = useMemo(() => {
      const query = new URLSearchParams(location.search)
      const parsed = Number.parseInt(query.get('max') ?? '20')
      return isNaN(parsed) ? Number.MAX_SAFE_INTEGER : parsed
   }, [location])

   const [search, setSearch] = useState('')
   const filtered = useMemo(() => {
      const withoutExluded = data?.filter(e => !EXCLUDED_EVENTS.includes(e.type))
      const terms = search
         .split(' ')
         .map(s => s.trim().toLowerCase())
         .filter(s => s.length > 0)
      if (terms.length === 0) return withoutExluded
      return withoutExluded?.filter(e => terms.every(term => e.type.includes(term) || searchRecursive(e.event, term)))
   }, [data, search])

   const grouped = useMemo(() => {
      const grouped = Object.entries(groupBy(filtered, e => e.gameTime))
      const sorted = grouped.map(([k, events]) => [k, orderBy(events, e => e.realTime)] as [typeof k, typeof events])
      return orderBy(sorted, e => -e[0]).slice(0, max)
   }, [filtered])

   return (
      <Page mini>
         <Input placeholder='Search...' value={search} onChange={e => setSearch(e.target.value)} />
         <List>
            {grouped.map(([key, events], i) => (
               <Group key={key}>
                  <Timestamp refresh={i <= 10 ? 10 : 60} time={events[0].realTime} />
                  <ul>
                     {events.map(e => (
                        <EventLine key={e.id} {...e} />
                     ))}
                  </ul>
               </Group>
            ))}
         </List>
      </Page>
   )
}

const Group = styled.ul`
   padding: 0.5rem 0;
   &:nth-of-type(odd) {
      background: ${p => darken(0.05, p.theme.bg)};
   }

   display: grid;
   align-items: center;
   grid-template-columns: 1fr 4fr;
`

const List = styled.ul`
   margin-top: 1rem;
   list-type: none;

   min-width: 800px;
`

export default Events
