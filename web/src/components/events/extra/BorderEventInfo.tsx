import { VFC } from 'react'
import { Colored } from '../../Text'
import { BorderEvent } from '../types'

const BorderEventInfo: VFC<BorderEvent> = ({ action }) => (
   <>
      Border started to <Colored>{action}</Colored>
   </>
)

export default BorderEventInfo
