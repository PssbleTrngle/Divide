import { FC, useCallback, useEffect } from 'react'
import styled from 'styled-components'
import useDialog from '../hooks/useDialog'
import useSubmit from '../hooks/useSubmit'
import useTooltip from '../hooks/useTooltip'
import ChoosePlayer from './dialogs/ChoosePlayer'
import ChooseTeam from './dialogs/ChooseTeam'
import { Indicator, Panel } from './Panels'
import type { Reward } from './Rewards'

const RewardPanel: FC<Reward> = ({ id, display, secret, target, ...props }) => {
   const buy = useSubmit(`buy/${id}`, { data: { target: '' }, keys: ['points'] })
   const { open } = useDialog()

   const click = useCallback(() => {
      if (target === 'player') return open(<ChoosePlayer {...buy} />)
      if (target === 'team') return open(<ChooseTeam {...buy} />)
      return buy.send()
   }, [target, buy])

   useEffect(() => {
      if (buy.error) console.error(buy.error)
   }, [buy.error])

   useTooltip()

   return (
      <Panel {...props} onBuy={click} key={display}>
         <Name>{display}</Name>
         {secret && <Indicator data-tip='secret'>👁</Indicator>}
      </Panel>
   )
}

const Name = styled.h3`
   text-align: left;
`

export default RewardPanel
