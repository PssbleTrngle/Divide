import { VFC } from 'react'
import { BorderEvent } from '../../../models/events'
import { Colored } from '../../Text'

const BorderEventInfo: VFC<BorderEvent> = ({ action }) => (
   <>
      Border started to <Colored>{action}</Colored>
   </>
)

export default BorderEventInfo
