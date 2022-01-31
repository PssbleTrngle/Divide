import { VFC } from 'react'
import { EraEvent } from '../../../models/events'
import { Colored } from '../../Text'

const EraEventInfo: VFC<EraEvent> = ({ era }) => (
   <>
      <Colored>{era}</Colored> era started
   </>
)

export default EraEventInfo
