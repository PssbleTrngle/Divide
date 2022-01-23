import { VFC } from 'react'
import { CycleEvent } from './types'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action, pause, timesRun }) => (
   <span>
      {id} {action}
   </span>
)

export default CycleEventInfo
