import { useEffect, VFC } from 'react'
import useSubmit from '../hooks/useSubmit'
import { Order } from './Orders'
import { Panel } from './Panels'

const OrderPanel: VFC<Order> = ({ id, cost, item }) => {
   const buy = useSubmit(`order/${id}`, { data: { amount: 1 } })

   useEffect(() => {
      if (buy.error) console.error(buy.error)
   }, [buy.error])

   return (
      <Panel price={cost} onBuy={buy.send}>
         {item}
      </Panel>
   )
}

export default OrderPanel
