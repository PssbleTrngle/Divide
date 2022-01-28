import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { BountyEvent } from '../types'

const BountyEventInfo: VFC<BountyEvent> = ({ bounty, fulfilledBy, pointsEarned }) => (
   <>
      <EventPlayer {...fulfilledBy} /> fulfilled <Colored>{bounty}</Colored> and gained{' '}
      <Colored>{pointsEarned}</Colored> points
   </>
)

export default BountyEventInfo
