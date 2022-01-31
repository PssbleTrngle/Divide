import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import { Reward } from '../models/rewards'
import Panels from './Panels'
import RewardPanel from './RewardPanel'
import { Colored } from './Text'

const Rewards: VFC = () => {
   const { data: points } = useApi<number>('points')
   const { data: rewards } = useResource<Reward>('reward')

   return (
      <Style>
         <Points>
            Your team has <Colored>{points}</Colored> points
         </Points>
         <Panels by={2}>
            {rewards?.map(reward => (
               <RewardPanel key={reward.id} {...reward} />
            ))}
         </Panels>
      </Style>
   )
}

const Points = styled.p``

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

export default Rewards
