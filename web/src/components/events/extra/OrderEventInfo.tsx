import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { OrderEvent } from '../types'

const OrderEventInfo: VFC<OrderEvent> = ({ orderedBy, amount, order, cost }) => (
   <span>
      <EventPlayer {...orderedBy} /> ordered{' '}
      <Colored>
         {amount} × {order.item}
      </Colored>{' '}
      for <Colored>{cost}</Colored> points
   </span>
)

export default OrderEventInfo
