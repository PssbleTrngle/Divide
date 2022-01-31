import { VFC } from 'react'
import { OrderEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'

const OrderEventInfo: VFC<OrderEvent> = ({ orderedBy, amount, order, cost }) => (
   <>
      <EventPlayer {...orderedBy} /> ordered{' '}
      <Colored>
         {amount} × {order.item}
      </Colored>{' '}
      for <Colored>{cost}</Colored> points
   </>
)

export default OrderEventInfo
