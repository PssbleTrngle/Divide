import { VFC } from 'react'
import { Target } from '../../models/events'
import { Player } from '../../models/player'
import EventPlayer from './EventPlayer'
import EventTeam from './EventTeam'

function isPlayer(t: Target): t is Player {
   return 'uuid' in t
}

const EventTarget: VFC<Target & { onlyHead?: boolean }> = target =>
   isPlayer(target) ? <EventPlayer {...target} /> : <EventTeam {...target} />

export default EventTarget
