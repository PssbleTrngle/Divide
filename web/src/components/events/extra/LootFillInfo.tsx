import { VFC } from 'react'
import { LootFillEvent } from '../types'

const LootFillInfo: VFC<LootFillEvent> = ({ pos, table }) => (
   <span>
      {pos.x}/{pos.y}/{pos.z} {table}
   </span>
)

export default LootFillInfo
