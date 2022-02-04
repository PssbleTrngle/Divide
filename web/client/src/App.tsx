import { useEffect } from 'react'
import { useQueryClient } from 'react-query'
import { useLocation } from 'react-router-dom'
import ReactTooltip from 'react-tooltip'
import styled from 'styled-components'
import Link from './components/Link'
import Messages from './components/Messages'
import useSession from './hooks/useSession'
import { useEvent, useEvents } from './hooks/useSocket'
import { Event } from './models/events'
import useRouter from './router'

function App() {
   const element = useRouter()
   const client = useQueryClient()
   const { loggedIn } = useSession()

   useEvents(event => {
      client.setQueryData<Event[]>('events', a => [...(a ?? []), event])
   })

   useEvent('points', ({ event }) => {
      client.setQueriesData<number>('points', event.now)
   })

   const { pathname } = useLocation()
   useEffect(() => {
      ReactTooltip.hide()
      ReactTooltip.rebuild()
   }, [pathname])

   return (
      <section>
         <ReactTooltip type='dark' effect='solid' />
         <Messages />
         {element}
         {loggedIn && <LoggedIn to='/logout'>You are logged in</LoggedIn>}
      </section>
   )
}

const LoggedIn = styled(Link)`
   position: fixed;
   bottom: 1em;
   left: 1em;
`

export default App
