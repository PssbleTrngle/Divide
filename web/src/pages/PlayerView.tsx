import { VFC } from 'react'
import styled from 'styled-components'
import Page from '../components/Page'
import Ranks from '../components/Ranks'
import SelectionBar from '../components/SelectionBar'
import Status from '../components/Status'
import Team from '../components/Team'

const PlayerView: VFC = () => (
   <Style>
      <Status />
      <Ranks />
      <Team />
      <Panel values={['rewards', 'orders']} />
   </Style>
)

const Panel = styled(SelectionBar)`
   grid-area: panel;
   width: 800px;
`

const Style = styled(Page)`
   gap: 1rem;
   align-items: flex-start;
   grid-template:
      'status status'
      'panel ranks'
      'panel team'
      'panel .';
`

export default PlayerView
