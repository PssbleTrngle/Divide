import { DispatchWithoutAction, FC, useMemo } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import Box from './Box'
import Button from './Button'

const Panels = styled.section`
   display: grid;
   gap: 1rem;
   grid-template-columns: repeat(2, 1fr);
`

export const Panel: FC<{ price: number; onBuy?: DispatchWithoutAction }> = ({ children, price, onBuy }) => {
   const { data: points } = useApi<number>('points')
   const canBuy = useMemo(() => price <= (points ?? 0), [price, points])

   return (
      <Style>
         {children}
         <Price>{price} points</Price>
         <Buy onClick={onBuy} disabled={!canBuy || !onBuy}>
            Buy
         </Buy>
      </Style>
   )
}

export const Indicator = styled.div`
   position: absolute;
   top: 0;
   left: 0;
   padding: 0 0.2em;
   cursor: help;

   opacity: 0.5;
   transition: opacity 0.1s ease;

   &:hover {
      opacity: 1;
   }
`

const Price = styled.small`
   grid-area: price;
`

const Buy = styled(Button)`
   grid-area: buy;
`

const Style = styled(Box)`
   justify-content: end;
   min-width: 200px;
   position: relative;
   overflow: hidden;

   grid-template:
      'content price buy'
      / 3fr 2fr 1fr;
`

export default Panels
