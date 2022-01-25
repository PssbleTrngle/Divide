import { useEffect, useState } from 'react'
import ReactTooltip from 'react-tooltip'
import useRouter from './router'

function App() {
   const element = useRouter()
   const [rendered, setRendered] = useState(false)

   useEffect(() => {
      setRendered(true)
   })

   return (
      <section>
         {rendered && <ReactTooltip type='dark' effect='solid' />}
         {element}
      </section>
   )
}

export default App
