import { useMemo, useState, VFC } from 'react'
import styled from 'styled-components'
import EventLine from '../components/events/EventLines'
import { Event } from '../components/events/types'
import Input from '../components/Input'
import Page from '../components/Page'
import useApi from '../hooks/useApi'

function searchRecursive<T>(value: T, term: string): boolean {
   return Object.values(value).some(v => {
      if (typeof v === 'string') return v.toLowerCase().includes(term)
      if (typeof v === 'number') return v.toString().includes(term)
      if (typeof v === 'object') return searchRecursive(v, term)
      return false
   })
}

const Events: VFC = () => {
   const { data } = useApi<Event[]>('events')

   const sorted = useMemo(() => data?.map((e, id) => ({ ...e, id }))?.sort((a, b) => b.realTime - a.realTime), [data])

   const [search, setSearch] = useState('')
   const filtered = useMemo(() => {
      const terms = search
         .split(' ')
         .map(s => s.trim().toLowerCase())
         .filter(s => s.length > 0)
      if (terms.length === 0) return sorted
      return sorted?.filter(e => terms.every(term => e.type.includes(term) || searchRecursive(e.event, term)))
   }, [sorted, search])

   return (
      <Page mini>
         <Input placeholder='Search...' value={search} onChange={e => setSearch(e.target.value)} />
         <List>
            {filtered?.map(e => (
               <EventLine key={e.id} {...e} />
            ))}
         </List>
      </Page>
   )
}

const List = styled.ul`
   margin-top: 1rem;
   list-type: none;

   min-width: 800px;
`

export default Events
