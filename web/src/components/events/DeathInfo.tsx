import { VFC } from 'react'
import { Colored } from '../Text'
import { DeathEvent } from './types'

const DeathInfo: VFC<DeathEvent> = ({ player, killer, source }) =>
   killer ? (
      <span>
         <Colored>{killer.name}</Colored> killed <Colored>{player.name}</Colored> using <Colored>{source}</Colored>
      </span>
   ) : (
      <span>
         <Colored>{player.name}</Colored> died to <Colored>{source}</Colored>
      </span>
   )

export default DeathInfo
