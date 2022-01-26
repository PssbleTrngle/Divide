import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import OrderPanel from './OrderPanel'
import Panels from './Panels'
import { Colored } from './Text'

export interface Order {
   id: string
   item: string
   cost: number
   max?: number
}

const Orders: VFC = () => {
   const { data: points } = useApi<number>('points')
   const { data: orders } = useResource<Order>('order')

   return (
      <Style>
         <p>
            Your team has <Colored>{points}</Colored> points
         </p>
         <Panels>
            {orders?.map(order => (
               <OrderPanel key={order.id} {...order} />
            ))}
         </Panels>
      </Style>
   )
}

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

export default Orders
