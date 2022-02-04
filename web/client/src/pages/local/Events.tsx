import { groupBy, orderBy } from 'lodash'
import { darken } from 'polished'
import { useMemo, useState, VFC } from 'react'
import { useLocation } from 'react-router-dom'
import AutoSizer from 'react-virtualized-auto-sizer'
import { VariableSizeList as VirtualList } from 'react-window'
import styled, { createGlobalStyle } from 'styled-components'
import BackLink from '../../components/BackLink'
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
      const parsed = Number.parseInt(query.get('max') ?? '')
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
      const grouped = Object.values(groupBy(filtered, e => e.gameTime))
      const sorted = grouped.map(events => orderBy(events, e => e.realTime))
      return orderBy(sorted, e => -e[0]).slice(0, max)
   }, [filtered, max])

   return (
      <Page center>
         <BackLink>back to overview</BackLink>
         <Global />
         <Search
            placeholder='Search for terms like "reward" or "death"'
            value={search}
            onChange={e => setSearch(e.target.value)}
         />
         <List>
            <AutoSizer>
               {style => (
                  <VirtualList {...style} itemCount={grouped.length} itemSize={i => grouped[i].length * 60}>
                     {({ index, style }) => {
                        const events = grouped[index]
                        return (
                           <Group style={style} odd={index % 2 === 1}>
                              <Timestamp refresh={index <= 10 ? 10 : 60} time={events[0].realTime} />
                              <ul>
                                 {events.map(e => (
                                    <EventLine key={e.id} {...e} />
                                 ))}
                              </ul>
                           </Group>
                        )
                     }}
                  </VirtualList>
               )}
            </AutoSizer>
         </List>
      </Page>
   )
}

const Search = styled(Input)`
   width: 500px;
   max-width: 90vw;
   margin: 0 auto;
`

const Global = createGlobalStyle`
   html, body {
     // overflow: hidden;
   }
`

const Group = styled.ul<{ odd?: boolean }>`
   padding: 0.5rem 0;
   background: ${p => (p.odd ? darken(0.05, p.theme.bg) : darken(0.02, p.theme.bg))};

   display: grid;
   align-items: center;
   grid-template-columns: 1fr 4fr;
`

const List = styled.section`
   margin-top: 2em;
   height: calc(100vh - 20rem);
   width: 100vw;
`

export default Events
