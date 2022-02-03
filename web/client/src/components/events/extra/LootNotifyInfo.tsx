import { VFC } from 'react'
import { LootNotifyEvent } from '../../../models/events'
import EventPos from '../EventPos'
import EventTeam from '../EventTeam'

const LootNotifyInfo: VFC<LootNotifyEvent> = ({ pos, team }) => (
   <>
      <EventTeam {...team} /> has been notified about loot at <EventPos {...pos} />
   </>
)

export default LootNotifyInfo
