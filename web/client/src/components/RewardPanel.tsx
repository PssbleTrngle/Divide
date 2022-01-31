import { FC, useCallback } from 'react'
import styled from 'styled-components'
import useDialog from '../hooks/useDialog'
import useSubmit from '../hooks/useSubmit'
import useTooltip from '../hooks/useTooltip'
import { Reward } from '../models/rewards'
import { formatDuration } from '../util'
import ChoosePlayer from './dialogs/ChoosePlayer'
import ChooseTeam from './dialogs/ChooseTeam'
import { Indicator, Info, Panel } from './Panels'

const RewardPanel: FC<Reward> = ({ id, display, secret, target, duration, charge, ...props }) => {
   const buy = useSubmit(`buy/${id}`, { data: { target: '' }, keys: ['points'] })
   const { open } = useDialog()

   const click = useCallback(() => {
      if (target === 'player') return open(<ChoosePlayer {...buy} />)
      if (target === 'team') return open(<ChooseTeam {...buy} />)
      return buy.send()
   }, [target, buy])

   useTooltip()

   return (
      <Panel {...props} onBuy={click} key={display}>
         <Name>{display}</Name>
         <Info>
            {duration && <span>Runs for {formatDuration(duration)}</span>}
            {charge && <span>Takes {formatDuration(charge)} to charge</span>}
         </Info>
         {secret && <Indicator data-tip='secret'>👁</Indicator>}
      </Panel>
   )
}

const Name = styled.h3`
   text-align: left;
`

export default RewardPanel
