import { VFC } from 'react'
import { DeathEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'

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
