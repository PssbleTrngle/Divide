import { VFC } from 'react'
import { Colored } from '../Text'
import { RewardEvent } from './types'

const RewardBoughtInfo: VFC<RewardEvent> = ({ reward, boughtBy, pointsPaid }) => (
   <span>
      <Colored>{boughtBy.name}</Colored> used <Colored>{pointsPaid}</Colored> points to buy <Colored>{reward}</Colored>
   </span>
)

export default RewardBoughtInfo
