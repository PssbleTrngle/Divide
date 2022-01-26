import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { RewardEvent } from '../types'

const RewardBoughtInfo: VFC<RewardEvent> = ({ reward, boughtBy, pointsPaid }) => (
   <span>
      <EventPlayer {...boughtBy} /> used <Colored>{pointsPaid}</Colored> points to buy <Colored>{reward}</Colored>
   </span>
)

export default RewardBoughtInfo
