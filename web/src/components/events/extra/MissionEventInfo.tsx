import { VFC } from 'react'
import { Colored } from '../../Text'
import { MissionEvent } from '../types'

const MissionEventInfo: VFC<MissionEvent> = ({ mission, action }) => (
   <span>
      Mission <Colored>{action}</Colored>: {mission.description}
   </span>
)

export default MissionEventInfo
