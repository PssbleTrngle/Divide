import { VFC } from 'react'
import { CycleEvent } from '../../../models/events'
import { Capitalize, Colored } from '../../Text'

const CycleEventInfo: VFC<CycleEvent> = ({ id, action }) => (
   <>
      <Capitalize>{action}</Capitalize> <Colored>{id}</Colored>
   </>
)

export default CycleEventInfo
