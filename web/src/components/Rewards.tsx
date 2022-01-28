import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import Panels from './Panels'
import RewardPanel from './RewardPanel'
import { Colored } from './Text'

export interface Reward {
   id: string
   display: string
   price: number
   description?: string
   duration?: number
   charge?: number
   target?: 'player' | 'team'
   secret?: boolean
}

const Rewards: VFC = () => {
   const { data: points } = useApi<number>('points')
   const { data: rewards } = useResource<Reward>('reward')

   return (
      <Style>
         <Points>
            Your team has <Colored>{points}</Colored> points
         </Points>
         <Panels>
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
