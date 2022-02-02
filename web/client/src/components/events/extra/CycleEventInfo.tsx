import { VFC } from 'react'
import { CycleEvent } from '../../../models/events'
import { Colored } from '../../Text'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action }) => (
   <>
      {action} <Colored>{id}</Colored>
   </>
)

export default CycleEventInfo
