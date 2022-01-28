import { useMemo, VFC } from 'react'
import useApi from '../hooks/useApi'
import useSubmit from '../hooks/useSubmit'
import useTooltip from '../hooks/useTooltip'
import { BoughtOrder, Order } from './Orders'
import { Info, Panel } from './Panels'

const OrderPanel: VFC<Order> = ({ id, cost, item, max }) => {
   const buy = useSubmit(`order/${id}`, { data: { amount: 1 }, keys: ['order/bought'] })
   const { data: bought } = useApi<BoughtOrder[]>('order/bought')
   const alreadyBought = useMemo(() => bought?.find(it => it.order.item === item)?.amount ?? 0, [bought])
   const disabled = useMemo(() => !!max && alreadyBought >= max, [alreadyBought])

   useTooltip()

   return (
      <Panel disabled={disabled} price={cost} onBuy={buy.send}>
         {item}
         {!!max && <Info>max. {max}</Info>}
      </Panel>
   )
}

export default OrderPanel
