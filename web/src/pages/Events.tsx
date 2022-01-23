import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import EventLine from '../components/events/EventLines'
import { Event } from '../components/events/types'
import Page from '../components/Page'
import useApi from '../hooks/useApi'

const Events: VFC = () => {
   const { data } = useApi<Event[]>('events')

   const sorted = useMemo(() => data?.map((e, id) => ({ ...e, id }))?.sort((a, b) => b.realTime - a.realTime), [data])

   return (
      <Page>
         <List>
            {sorted?.map(e => (
               <EventLine key={e.id} {...e} />
            ))}
         </List>
      </Page>
   )
}

const List = styled.ul`
   list-type: none;
`

export default Events
