import { VFC } from 'react'
import { Player } from '../../models/player'
import { colorOf } from '../../util'
import PlayerHead from '../PlayerHead'
import { Colored } from '../Text'

const EventPlayer: VFC<Player & { color?: string }> = player => {
   const color = player.color ?? colorOf(player.team?.color)
   return (
      <>
         <PlayerHead highlight={color} {...player} size='1.4em' />
         <Colored color={color}>{player.name}</Colored>
      </>
   )
}

export default EventPlayer
