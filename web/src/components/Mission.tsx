import { VFC } from 'react'
import styled from 'styled-components'
import { biColorGradient } from '../styles/mixins'
import { formatDuration } from '../util'
import EventBanner from './EventBanner'

export interface MissionStatus {
   mission: Mission
   secondsLeft: number
   done: boolean
}

export interface Mission {
   description: string
}

const MissionInfo: VFC<MissionStatus> = ({ mission, done, secondsLeft }) =>
   done ? null : (
      <Style>
         Mission: {mission.description} {formatDuration(secondsLeft)}
      </Style>
   )

const Style = styled(EventBanner)`
   ${p => biColorGradient(p.theme.warning, p.theme.error)};
`

export default MissionInfo
