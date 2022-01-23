import { VFC } from 'react'
import styled from 'styled-components'

export interface Mission {
   description: string
}

const MissionInfo: VFC<Mission> = ({ description }) => <Style>Mission: {description}</Style>

const Style = styled.p`
   background: ${p => p.theme.warning};
   padding: 0.1em;
`

export default MissionInfo
