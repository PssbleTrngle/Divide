import { VFC } from 'react'
import { Colored } from '../../Text'
import { CycleEvent } from '../types'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action, pause, timesRun }) => (
   <span>
      {action} <Colored>{id}</Colored>
   </span>
)

export default CycleEventInfo
