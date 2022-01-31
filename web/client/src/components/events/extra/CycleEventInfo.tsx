import { VFC } from 'react'
import { CycleEvent } from '../../../models/events'
import { Colored } from '../../Text'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action, pause, timesRun }) => (
   <>
      {action} <Colored>{id}</Colored>
   </>
)

export default CycleEventInfo
