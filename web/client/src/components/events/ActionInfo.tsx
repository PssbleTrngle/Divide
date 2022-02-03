import { VFC } from 'react'
import { ActionEvent } from '../../models/events'
import { Capitalize, Colored } from '../Text'
import EventPlayer from './EventPlayer'
import EventTarget from './EventTarget'

const ActionInfo: VFC<ActionEvent> = ({ reward, action, target, boughtBy }) => (
   <>
      <Capitalize>{action}</Capitalize> reward <Colored>{reward}</Colored>{' '}
      {target ? (
         <>
            targeting <EventTarget {...target} />
         </>
      ) : null}
      bought by <EventPlayer {...boughtBy} onlyHead />
   </>
)

export default ActionInfo
