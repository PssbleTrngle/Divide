import { DateTime, Duration, DurationUnit } from 'luxon'
import { createElement, memo, VFC } from 'react'
import styled, { DefaultTheme, keyframes } from 'styled-components'
import CycleEventInfo from './CycleEventInfo'
import LootFillInfo from './LootFillInfo'
import { Event, EventType, EventTypes } from './types'

const Info: {
   [T in EventType]?: VFC<EventTypes[T]>
} = {
   loot_crate_filled: LootFillInfo,
   cycle_event: CycleEventInfo,
}

const UNITS: (DurationUnit & keyof Duration)[] = ['hours', 'minutes', 'seconds']

const EventLine = memo(function <T extends EventType>({ type, realTime, event }: Event<T>) {
   const time = DateTime.fromMillis(realTime)
   const diff = time.diffNow([...UNITS, 'milliseconds'])
   const since = UNITS.filter(u => diff[u] !== 0)
      .map(u => `${-diff[u]}${u.charAt(0)}`)
      .join(' ')

   const info = Info[type] as VFC<EventTypes[T]>

   return (
      <Style>
         <Type>{type}</Type>
         <span title={time.toLocaleString(DateTime.DATETIME_SHORT)}>{since.length ? since : 'moments'} ago</span>
         {info ? createElement(info, event) : <span>No Info</span>}
      </Style>
   )
})

const shine = (p: { theme: DefaultTheme }) => keyframes`
   from { background: ${p.theme.primary} }
   to { background:  transparent }
`

const Style = styled.li`
   display: grid;
   grid-template-columns: 2fr 3fr 6fr;
   align-items: center;
   gap: 0.5em;
   animation: ${shine} 1s ease-out;
   padding: 0.5em;
   margin: 0.1em 0;
   border-radius: 0.4em;
`

const Type = styled.span`
   background: ${p => p.theme.primary};
   border-radius: 0.4em;
   padding: 0.4em;
`

export default EventLine
