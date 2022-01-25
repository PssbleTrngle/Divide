import { FC, useCallback, useEffect } from 'react'
import styled from 'styled-components'
import useDialog from '../hooks/useDialog'
import useSubmit from '../hooks/useSubmit'
import Box from './Box'
import Button from './Button'
import ChoosePlayer from './dialogs/ChoosePlayer'
import ChooseTeam from './dialogs/ChooseTeam'
import type { Reward } from './Rewards'

const RewardPanel: FC<
   Reward & {
      canBuy: boolean
      id: string
   }
> = ({ id, display, price, canBuy, secret, target, ...props }) => {
   const buy = useSubmit(`buy/${id}`, { method: 'POST', data: { target: '' }, keys: ['points'] })
   const { open } = useDialog()

   const click = useCallback(() => {
      if (target === 'player') return open(<ChoosePlayer {...buy} />)
      if (target === 'team') return open(<ChooseTeam {...buy} />)
      return buy.send()
   }, [target, buy])

   useEffect(() => {
      if (buy.error) console.error(buy.error)
   }, [buy.error])

   return (
      <Style {...props} key={display}>
         <Name>{display}</Name>
         <small>{price} points</small>
         <Button onClick={click} disabled={!canBuy}>
            Buy
         </Button>
         {secret && <Indicator data-tip='secret'>👁</Indicator>}
      </Style>
   )
}

const Indicator = styled.div`
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

const Name = styled.h3`
   grid-area: name;
   text-align: left;
`

const Style = styled(Box)`
   justify-content: end;
   min-width: 200px;
   position: relative;
   overflow: hidden;

   grid-template:
      'name price buy'
      / 3fr 2fr 1fr;
`

export default RewardPanel
