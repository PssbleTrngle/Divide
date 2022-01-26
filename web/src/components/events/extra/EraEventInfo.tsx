import { VFC } from 'react'
import { Colored } from '../../Text'
import { EraEvent } from '../types'

const EraEventInfo: VFC<EraEvent> = ({ era }) => (
   <span>
      <Colored>{era}</Colored> era started
   </span>
)

export default EraEventInfo
