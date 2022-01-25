import { VFC } from 'react'
import styled from 'styled-components'
import Page from '../components/Page'
import Ranks from '../components/Ranks'
import Status from '../components/Status'
import Teams from '../components/Teams'

const SpectatorView: VFC = () => (
   <Style>
      <Status />
      <Ranks />
      <Teams />
   </Style>
)

const Style = styled(Page)`
   gap: 1rem;
   align-items: flex-start;
   grid-template:
      'status status'
      'teams ranks'
      'teams .';
`

export default SpectatorView
