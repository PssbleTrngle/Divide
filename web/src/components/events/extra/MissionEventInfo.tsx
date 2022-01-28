import { VFC } from 'react'
import { Colored } from '../../Text'
import EventTeam from '../EventTeam'
import { MissionEvent } from '../types'

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
