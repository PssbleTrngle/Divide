import { VFC } from 'react'
import styled from 'styled-components'
import { biColorGradient } from '../styles/mixins'
import EventBanner from './EventBanner'

export interface Mission {
   description: string
}

const MissionInfo: VFC<Mission> = ({ description }) => <Style>Mission: {description}</Style>

const Style = styled(EventBanner)`
   ${p => biColorGradient(p.theme.warning, p.theme.error)};
`

export default MissionInfo
