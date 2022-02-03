import { VFC } from 'react'
import { LootEvent } from '../../../models/events'
import EventPos from '../EventPos'

const LootCleanInfo: VFC<LootEvent> = ({ pos }) => (
   <>
      Loot at <EventPos {...pos} /> has been cleaned
   </>
)

export default LootCleanInfo
