import { VFC } from 'react'
import { RewardEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import EventTarget from '../EventTarget'

const RewardBoughtInfo: VFC<RewardEvent> = ({ reward, boughtBy, pointsPaid, target }) => (
   <>
      <EventPlayer {...boughtBy} /> used <Colored>{pointsPaid}</Colored> points to buy <Colored>{reward}</Colored>{' '}
      {target && (
         <>
            targeting <EventTarget onlyHead {...target} />
         </>
      )}
   </>
)

export default RewardBoughtInfo
