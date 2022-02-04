import { VFC } from 'react'
import { useLocation } from 'react-router-dom'
import Link from '../components/Link'
import Page from '../components/Page'
import { Title } from '../components/Text'

const Forbidden: VFC = () => {
   const { pathname } = useLocation()
   return (
      <Page center>
         <section>
            <Title>403 - Forbidden</Title>
            <Link to={`/login?redirect=${pathname}`}>login to view this</Link>
         </section>
      </Page>
   )
}

export default Forbidden
