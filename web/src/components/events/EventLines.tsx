import { DateTime, Duration, DurationUnit } from 'luxon'
import { invert } from 'polished'
import { createElement, memo, useEffect, useState, VFC } from 'react'
import styled, { DefaultTheme, keyframes } from 'styled-components'
import ActionInfo from './ActionInfo'
import CycleEventInfo from './CycleEventInfo'
import DeathInfo from './DeathInfo'
import GameEventInfo from './GameEventInfo'
import LootFillInfo from './LootFillInfo'
import RewardBoughtInfo from './RewardBoughtInfo'
import { Event, EventType, EventTypes } from './types'

const Info: {
   [T in EventType]?: VFC<EventTypes[T]>
} = {
   loot_crate_filled: LootFillInfo,
   cycle_event: CycleEventInfo,
   death: DeathInfo,
   reward: RewardBoughtInfo,
   action: ActionInfo,
   game: GameEventInfo,
}

const EventLine = memo(function <T extends EventType>({ type, realTime, event }: Event<T>) {
   const info = Info[type] as VFC<EventTypes[T]>

   return (
      <Style>
         <Type>{type}</Type>
         <Timestamp time={realTime} />
         <Extra>{info ? createElement(info, event) : <i>No Info</i>}</Extra>
      </Style>
   )
})

const UNITS: (DurationUnit & keyof Duration)[] = ['hours', 'minutes', 'seconds']

const Timestamp: VFC<{ time: number }> = props => {
   const [now, updateNow] = useState(DateTime.now())
   const time = DateTime.fromMillis(props.time)
   const diff = time.diff(now, [...UNITS, 'milliseconds'])
   const since = UNITS.filter(u => diff[u] !== 0)
      .map(u => `${-diff[u]}${u.charAt(0)}`)
      .join(' ')

   useEffect(() => {
      const interval = setInterval(() => updateNow(DateTime.now()), 1000 * 10)
      return () => clearInterval(interval)
   }, [])

   return <span data-tip={time.toLocaleString(DateTime.DATETIME_SHORT)}>{since.length ? since : 'moments'} ago</span>
}

const Extra = styled.span`
   text-align: left;
`

const shine = (p: { theme: DefaultTheme }) => keyframes`
   from { background: ${p.theme.primary} }
   to { background:  transparent }
`

const Style = styled.li`
   display: grid;
   grid-template-columns: 2fr 3fr 6fr;
   align-items: center;
   gap: 0.5em;
   //animation: ${shine} 1s ease-out;
   padding: 0.5em;
   margin: 0.1em 0;
   border-radius: 0.4em;
`

const Type = styled.span`
   background: ${p => p.theme.primary};
   color: ${p => invert(p.theme.text)};
   border-radius: 0.4em;
   padding: 0.4em;
`

export default EventLine
