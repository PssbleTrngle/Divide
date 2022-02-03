import { VFC } from 'react'
import { LootFillEvent } from '../../../models/events'
import EventPos from '../EventPos'

const LootFillInfo: VFC<LootFillEvent> = ({ pos }) => (
   <>
      Loot has appeared at <EventPos {...pos} />
   </>
)

export default LootFillInfo
