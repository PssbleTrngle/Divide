import { VFC } from 'react'
import Page from '../components/Page'
import Rewards from '../components/Rewards'
import Status from '../components/Status'
import Team from '../components/Team'
import useSession from '../hooks/useSession'

const Home: VFC = () => {
   const { player } = useSession()
   return (
      <Page>
         <h1>Logged in as {player.name}</h1>
         <Status />
         <Team />
         <Rewards />
      </Page>
   )
}

export default Home
