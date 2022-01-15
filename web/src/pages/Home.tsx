import { VFC } from 'react'
import styled from 'styled-components'
import Page from '../components/Page'
import Ranks from '../components/Ranks'
import Rewards from '../components/Rewards'
import Status from '../components/Status'
import Team from '../components/Team'

const Home: VFC = () => {
   return (
      <Style>
         <Status />
         <Team />
         <Ranks />
         <Rewards />
      </Style>
   )
}

const Style = styled(Page)`
   gap: 1rem;
   grid-template:
      'status status'
      'rewards ranks'
      'rewards team';
`

export default Home
