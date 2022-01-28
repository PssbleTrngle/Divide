import { VFC } from 'react'
import { Colored } from '../../Text'
import { EraEvent } from '../types'

const EraEventInfo: VFC<EraEvent> = ({ era }) => (
   <>
      <Colored>{era}</Colored> era started
   </>
)

export default EraEventInfo
