import { VFC } from 'react'
import Page from '../components/Page'
import { Code, Title } from '../components/Text'

const LoggedOut: VFC = () => (
   <Page center>
      <section>
         <Title>You are logged out</Title>
         <p>
            Use <Code>/web</Code> in-game to retrieve login link
         </p>
      </section>
   </Page>
)

export default LoggedOut
