import ReactTooltip from 'react-tooltip'
import Messages from './components/Messages'
import useRouter from './router'

function App() {
   const element = useRouter()

   return (
      <section>
         <ReactTooltip type='dark' effect='solid' />
         <Messages />
         {element}
      </section>
   )
}

export default App
