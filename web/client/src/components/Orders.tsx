import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useResource from '../hooks/useResource'
import useTooltip from '../hooks/useTooltip'
import { BoughtOrder, Order } from '../models/orders'
import Box from './Box'
import ItemIcon from './ItemIcon'
import OrderPanel from './OrderPanel'
import Panels from './Panels'
import { Colored } from './Text'

const Orders: VFC = () => {
   const { data: points } = useApi<number>('points')
   const { data: orders } = useResource<Order>('order')
   const { data: bought } = useApi<BoughtOrder[]>('order/bought')

   useTooltip()

   return (
      <Style>
         <p>
            Your team has <Colored>{points}</Colored> points
         </p>
         <Box>
            {bought?.length ? (
               <>
                  <small>
                     <i>already ordered</i>
                  </small>
                  <BoughtOrders>
                     {bought?.map(({ order, amount }) => (
                        <BoughtOrderPanel data-tip={order.item} key={order.item}>
                           <span>{amount} ×</span>
                           <ItemIcon {...order} />
                        </BoughtOrderPanel>
                     ))}
                  </BoughtOrders>
               </>
            ) : (
               <p>not ordered anything yet</p>
            )}
         </Box>
         <Panels by={2}>
            {orders?.map(order => (
               <OrderPanel key={order.id} {...order} />
            ))}
         </Panels>
      </Style>
   )
}

const BoughtOrders = styled.section`
   display: grid;
   grid-template-columns: repeat(auto-fill, 4em);
   grid-auto-flow: column;
   gap: 1em;
`

const BoughtOrderPanel = styled.div`
   display: grid;
   grid-template-columns: 1fr 1fr;
   justify-content: center;
   align-items: center;
`

const Style = styled.section`
   display: grid;
   gap: 1rem;
`

export default Orders
