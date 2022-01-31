import { useQueryClient } from 'react-query'
import ReactTooltip from 'react-tooltip'
import Messages from './components/Messages'
import { useEvent, useEvents } from './hooks/useSocket'
import { Event } from './models/events'
import useRouter from './router'

function App() {
   const element = useRouter()
   const client = useQueryClient()

   useEvents(event => {
      client.setQueryData<Event[]>('events', a => [...(a ?? []), event])
   })

   useEvent('points', ({ event }) => {
      client.setQueriesData<number>('points', event.now)
   })

   return (
      <section>
         <ReactTooltip type='dark' effect='solid' />
         <Messages />
         {element}
      </section>
   )
}

export default App
