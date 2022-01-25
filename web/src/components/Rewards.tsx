import { useEffect, VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import useSubmit from '../hooks/useSubmit'
import Box from './Box'
import Button from './Button'
import { GameStatus } from './Status'
import { Colored } from './Text'

interface Reward {
   display: string
   price: number
   description?: string
   duration?: number
   requiresTarget?: boolean
}

const Rewards: VFC = () => {
   const { data: status } = useApi<GameStatus>('status')
   const { data: rewards, error } = useResource<{ reward: Reward; target: string }>('reward')

   console.log(error)

   return (
      <Style>
         <Points>
            Your team has <Colored>{status?.points}</Colored> points
         </Points>
         <Panels>
            {rewards?.map(({ value: { reward, target }, id }) => (
               <RewardPanel key={id} {...reward} id={id} canBuy={reward.price <= (status?.points ?? 0)} />
            ))}
         </Panels>
      </Style>
   )
}

const Points = styled.p``

const RewardPanel: VFC<Reward & { canBuy: boolean; id: string }> = ({ id, display: name, price, canBuy }) => {
   const buy = useSubmit(`buy/${id}`, { method: 'POST', data: { target: '' } })

   useEffect(() => {
      if (buy.error) console.error(buy.error)
   }, [buy.error])

   return (
      <Panel key={name}>
         <Name>{name}</Name>
         <small>{price} points</small>
         <Button onClick={buy.send} disabled={!canBuy}>
            Buy
         </Button>
      </Panel>
   )
}

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

const Name = styled.h3`
   grid-area: name;
   text-align: left;
`

const Panel = styled(Box)`
   justify-content: end;
   min-width: 200px;

   grid-template:
      'name price buy'
      / 3fr 1fr 1fr;
`

export default Rewards
