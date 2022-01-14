import { darken } from 'polished'
import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import Button from './Button'

interface Reward {
   display: string
   price: number
   description?: string
   duration?:  number
   requiresTarget?: boolean
}

const Rewards: VFC = () => {
   const { data } = useApi<Reward[]>('reward')

   return (
      <Style>
         {data?.map(({ display: name, description, price }) => (
            <Panel key={name}>
               <h3>{name}</h3>
               <small>{price} smackles</small>
               <Desc>{description}</Desc>
               <Button>Buy</Button>
            </Panel>
         ))}
      </Style>
   )
}

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

const Desc = styled.p`
   grid-area: desc;
`

const Panel = styled.div`
   border-radius: 1rem;
   background: ${p => darken(0.05, p.theme.bg)};
   padding: 0.5rem;

   display: grid;
   align-items: center;

   grid-template:
      'name price buy'
      'desc desc desc';
`

export default Rewards
