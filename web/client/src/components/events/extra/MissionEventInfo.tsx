import { VFC } from 'react'
import { MissionEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventTeam from '../EventTeam'

const MissionEventInfo: VFC<MissionEvent> = ({ mission, action, team }) =>
   team ? (
      <>
         <EventTeam {...team} /> {action} mission <Colored>{mission.description}</Colored>
      </>
   ) : (
      <>
         Mission {action}: <Colored>{mission.description}</Colored>
      </>
   )

export default MissionEventInfo
