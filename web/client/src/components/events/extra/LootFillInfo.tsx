import { VFC } from 'react'
import { LootFillEvent } from '../../../models/events'

const LootFillInfo: VFC<LootFillEvent> = ({ pos, table }) => (
   <>
      {pos.x}/{pos.y}/{pos.z} {table}
   </>
)

export default LootFillInfo
