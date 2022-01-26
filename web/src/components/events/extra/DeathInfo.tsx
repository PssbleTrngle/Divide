import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { DeathEvent } from '../types'

const DeathInfo: VFC<DeathEvent> = ({ player, killer, source }) =>
   killer ? (
      <span>
         <EventPlayer {...killer} /> killed <EventPlayer {...player} /> using <Colored>{source}</Colored>
      </span>
   ) : (
      <span>
         <EventPlayer {...player} /> died to <Colored>{source}</Colored>
      </span>
   )

export default DeathInfo
