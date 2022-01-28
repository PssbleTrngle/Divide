import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { DeathEvent } from '../types'

const DeathInfo: VFC<DeathEvent> = ({ player, killer, source }) =>
   killer ? (
      <>
         <EventPlayer {...killer} /> killed <EventPlayer {...player} /> using <Colored>{source}</Colored>
      </>
   ) : (
      <>
         <EventPlayer {...player} /> died to <Colored>{source}</Colored>
      </>
   )

export default DeathInfo
