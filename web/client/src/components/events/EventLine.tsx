import { createElement, memo, VFC } from 'react'
import styled, { DefaultTheme, keyframes } from 'styled-components'
import { Event, EventType, EventTypes } from '../../models/events'
import { Inline } from '../Text'
import ActionInfo from './ActionInfo'
import BorderEventInfo from './extra/BorderEventInfo'
import BountyEventInfo from './extra/BountyEventInfo'
import CycleEventInfo from './extra/CycleEventInfo'
import DeathInfo from './extra/DeathInfo'
import EraEventInfo from './extra/EraEventInfo'
import GameEventInfo from './extra/GameEventInfo'
import LootCleanInfo from './extra/LootCleanInfo'
import LootFillInfo from './extra/LootFillInfo'
import LootNotifyInfo from './extra/LootNotifyInfo'
import MissionEventInfo from './extra/MissionEventInfo'
import OrderEventInfo from './extra/OrderEventInfo'
import PointsEventInfo from './extra/PointsEventInfo'
import RewardBoughtInfo from './extra/RewardBoughtInfo'
import ScoreEventInfo from './extra/ScoreEventInfo'

const Info: {
   [T in EventType]?: VFC<EventTypes[T]>
} = {
   loot_crate_filled: LootFillInfo,
   loot_crate_notify: LootNotifyInfo,
   loot_crate_cleaned: LootCleanInfo,
   cycle_event: CycleEventInfo,
   death: DeathInfo,
   reward: RewardBoughtInfo,
   action: ActionInfo,
   game: GameEventInfo,
   border: BorderEventInfo,
   eras: EraEventInfo,
   mission: MissionEventInfo,
   bounty: BountyEventInfo,
   order: OrderEventInfo,
   points: PointsEventInfo,
   score: ScoreEventInfo,
}

const EventLine = memo(function <T extends EventType>({ type, event, ...props }: Event<T>) {
   const info = Info[type] as VFC<EventTypes[T]>
   return (
      <Style as='li' {...props}>
         {info ? createElement(info, event) : <i data-tip={JSON.stringify(event, null, 2)}>{type}</i>}
      </Style>
   )
})

const shine = (p: { theme: DefaultTheme }) => keyframes`
   from { background: ${p.theme.primary} }
   to { background:  transparent }
`

const Style = styled(Inline)`
   //animation: ${shine} 1s ease-out;
   padding: 0.5em;
   margin: 0.1em 0;
   border-radius: 0.4em;
`

export default EventLine
