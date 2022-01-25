import { DispatchWithoutAction, VFC } from 'react'
import styled from 'styled-components'
import Button from '../components/Button'
import Page from '../components/Page'
import { Code, Title } from '../components/Text'

const LoggedOut: VFC<{ onSpectate?: DispatchWithoutAction }> = ({ onSpectate }) => (
   <Page center>
      <section>
         <Title>You are logged out</Title>
         <p>
            Use <Code>/web</Code> in-game to retrieve login link
         </p>
         {onSpectate && (
            <Spectate>
               <span>or </span>
               <Button onClick={onSpectate}>Join as Spectator</Button>
            </Spectate>
         )}
      </section>
   </Page>
)

const Spectate = styled.p`
   margin-top: 2em;
   display: grid;
   grid-auto-flow: column;
   align-items: center;
   justify-content: center;
   gap: 0.5em;
`

export default LoggedOut
