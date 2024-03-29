import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { GameStatus } from '../models/game'
import { biColorGradient } from '../styles/mixins'
import { formatDuration } from '../util'
import EventBanner from './EventBanner'
import MissionInfo from './Mission'

const Status: VFC = () => {
   const { data } = useApi<GameStatus>('status')

   return (
      <Style>
         {data?.peaceUntil && <Peace>Peace {formatDuration(data.peaceUntil)}</Peace>}
         {data?.mission && <MissionInfo {...data.mission} />}
      </Style>
   )
}

const Peace = styled(EventBanner)`
   ${p => biColorGradient(p.theme.ok, p.theme.warning)};
`

const Style = styled.section`
   grid-area: status;
   display: grid;
   gap: 1rem;
   margin-bottom: 2rem;
`

export default Status
