import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import RewardPanel from './RewardPanel'
import { Colored } from './Text'

export interface Reward {
   display: string
   price: number
   description?: string
   duration?: number
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
            {rewards?.map(({ value: reward, id }) => (
               <RewardPanel key={id} {...reward} id={id} canBuy={reward.price <= (points ?? 0)} />
            ))}
         </Panels>
      </Style>
   )
}

const Points = styled.p``

const Panels = styled.section`
   display: grid;
   gap: 1rem;
   grid-template-columns: repeat(2, 1fr);
`

const Style = styled.section`
   grid-area: rewards;
   display: grid;
   gap: 1rem;
`

export default Rewards
