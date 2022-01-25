import { useEffect, useState } from 'react'
import { useRoutes } from 'react-router-dom'
import ReactTooltip from 'react-tooltip'
import routes from './router'

function App() {
   const element = useRoutes(routes)
   const [rendered, setRendered] = useState(false)

   useEffect(() => {
      setRendered(true)
   }, [])

   return (
      <section>
         {rendered && <ReactTooltip type='dark' effect='solid' />}
         {element}
      </section>
   )
}

export default App
