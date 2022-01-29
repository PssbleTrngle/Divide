import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import useSubmit from '../hooks/useSubmit'
import useTooltip from '../hooks/useTooltip'
import ItemIcon from './ItemIcon'
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
         <ItemBox>
            <ItemIcon data-tip={item} item={item} />
            {item}
         </ItemBox>
         {!!max && <Info>max. {max}</Info>}
      </Panel>
   )
}

const ItemBox = styled.div`
   display: grid;
   grid-auto-flow: column;
   align-items: center;
   justify-content: start;
   gap: 1em;
`

export default OrderPanel
