import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import MissionInfo, { Mission } from './Mission'

export interface GameStatus {
   peaceUntil?: number
   points: number
   mission?: Mission
   paused: boolean
   started: boolean
}

const Status: VFC = () => {
   const { data } = useApi<GameStatus>('status')

   const isPeace = useMemo(() => !!data?.peaceUntil, [data])

   return (
      <Style>
         {isPeace && <Peace>Peace {data?.peaceUntil}s</Peace>}
         <p>{data?.points} points</p>
         {data?.mission && <MissionInfo {...data.mission} />}
      </Style>
   )
}

const Peace = styled.section`
   background: ${p => p.theme.ok};
   color: black;
   padding: 0.5em;
   border-radius: 1em;
`

const Style = styled.section`
   grid-area: status;
   display: grid;
   gap: 1rem;
`

export default Status
