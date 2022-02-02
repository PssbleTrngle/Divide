import { VFC } from 'react'
import { LootFillEvent } from '../../../models/events'
import { Colored } from '../../Text'

const LootFillInfo: VFC<LootFillEvent> = ({ pos }) => (
   <>
      Loot has appeared at{' '}
      <Colored>
         {pos.x}/{pos.y}/{pos.z}
      </Colored>
   </>
)

export default LootFillInfo
