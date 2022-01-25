import { VFC } from 'react'
import styled from 'styled-components'
import EventBanner from './EventBanner'

export interface Mission {
   description: string
}

const MissionInfo: VFC<Mission> = ({ description }) => <Style>Mission: {description}</Style>

const Style = styled(EventBanner)`
   background: ${p => p.theme.warning};
`

export default MissionInfo
