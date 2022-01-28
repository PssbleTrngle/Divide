import { VFC } from 'react'
import { Colored } from '../../Text'
import { CycleEvent } from '../types'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action, pause, timesRun }) => (
   <>
      {action} <Colored>{id}</Colored>
   </>
)

export default CycleEventInfo
