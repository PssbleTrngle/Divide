import { useMemo, VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import EventBanner from './EventBanner'
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
         {data?.mission && <MissionInfo {...data.mission} />}
      </Style>
   )
}

const Peace = styled(EventBanner)`
   background: ${p => p.theme.ok};
`

const Style = styled.section`
   grid-area: status;
   display: grid;
   gap: 1rem;
   margin-bottom: 2rem;
`

export default Status
