import { darken } from 'polished'
import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import useSubmit from '../hooks/useSubmit'
import Button from './Button'
import { GameStatus } from './Status'

interface Reward {
   display: string
   price: number
   description?: string
   duration?: number
   requiresTarget?: boolean
}

const Rewards: VFC = () => {
   const { data: status } = useApi<GameStatus>('status')
   const { data: rewards } = useResource<Reward>('reward')

   return (
      <Style>
         {rewards?.map(({ value: reward, id }) => (
            <RewardPanel key={id} {...reward} id={id} canBuy={reward.price <= (status?.points ?? 0)} />
         ))}
      </Style>
   )
}

const RewardPanel: VFC<Reward & { canBuy: boolean; id: string }> = ({ id, display: name, price, canBuy }) => {
   const buy = useSubmit(`buy/${id}`, { method: 'POST' })

   return (
      <Panel key={name}>
         <Name>{name}</Name>
         <small>{price} points</small>
         <Button onClick={buy} disabled={!canBuy}>
            Buy
         </Button>
      </Panel>
   )
}

const Style = styled.section`
   min-width: 300px;
   grid-area: rewards;
   display: grid;
   gap: 1rem;
`

const Name = styled.h3`
   grid-area: name;
   text-align: left;
`

const Panel = styled.div`
   border-radius: 1em;
   background: ${p => darken(0.05, p.theme.bg)};
   padding: 0.5em 1em;
   gap: 1em;

   display: grid;
   align-items: center;
   justify-content: end;

   grid-template:
      'name price buy'
      / 3fr 1fr 1fr;
`

export default Rewards
