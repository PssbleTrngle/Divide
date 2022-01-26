import { VFC } from 'react'
import { Colored } from '../../Text'
import { BorderEvent } from '../types'

const BorderEventInfo: VFC<BorderEvent> = ({ action }) => (
   <span>
      Border started to <Colored>{action}</Colored>
   </span>
)

export default BorderEventInfo
