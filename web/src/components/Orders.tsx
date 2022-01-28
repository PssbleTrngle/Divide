import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import Box from './Box'
import OrderPanel from './OrderPanel'
import Panels from './Panels'
import { Colored } from './Text'

export interface Order {
   id: string
   item: string
   cost: number
   max?: number
}

export interface BoughtOrder {
   order: Order
   amount: number
}

const Orders: VFC = () => {
   const { data: points } = useApi<number>('points')
   const { data: orders } = useResource<Order>('order')
   const { data: bought } = useApi<BoughtOrder[]>('order/bought')

   return (
      <Style>
         <p>
            Your team has <Colored>{points}</Colored> points
         </p>
         <Box>
            {bought?.length ? (
               bought?.map(({ order, amount }) => (
                  <p key={order.item}>
                     {order.item}: {amount}
                  </p>
               ))
            ) : (
               <p>not ordered anything yet</p>
            )}
         </Box>
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
