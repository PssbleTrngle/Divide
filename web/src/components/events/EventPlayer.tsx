import { VFC } from 'react'
import styled from 'styled-components'
import { Player } from '../../hooks/useSession'
import { colorOf } from '../../util'
import PlayerHead from '../PlayerHead'
import { Colored } from '../Text'

const EventPlayer: VFC<Player> = player => {
   const color = colorOf(player.team?.color)
   return (
      <>
         <InlineHead highlight={color} {...player} size='1.4em' />
         <Colored color={color}>{player.name}</Colored>
      </>
   )
}

const InlineHead = styled(PlayerHead)`
   margin-right: -0.1em;
`

export default EventPlayer
