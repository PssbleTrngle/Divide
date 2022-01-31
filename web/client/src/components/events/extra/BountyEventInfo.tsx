import { VFC } from 'react'
import { BountyEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'

const BountyEventInfo: VFC<BountyEvent> = ({ bounty, fulfilledBy, pointsEarned }) => (
   <>
      <EventPlayer {...fulfilledBy} /> fulfilled <Colored>{bounty.description}</Colored> and gained{' '}
      <Colored>{pointsEarned}</Colored> points
   </>
)

export default BountyEventInfo
