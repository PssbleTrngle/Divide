import { VFC } from 'react'
import styled from 'styled-components'
import { Player } from '../../hooks/useSession'
import { colorOf } from '../../util'
import PlayerHead from '../PlayerHead'
import { Colored } from '../Text'

const EventPlayer: VFC<Player> = player => (
   <span>
      <InlineHead highlight={colorOf(player.team?.color)} {...player} size='1.4em' />
      <Colored>{player.name}</Colored>
   </span>
)

const InlineHead = styled(PlayerHead)`
   display: inline-block;
   margin-right: 0.25em;
   align-self: center;
   float: left;
`

export default EventPlayer
