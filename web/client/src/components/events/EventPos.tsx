import { VFC } from 'react'
import { Position } from '../../models/events'
import { Colored } from '../Text'

const EventPos: VFC<Position> = ({ x, y, z, dimension }) => (
   <Colored data-tip={`in ${dimension}`}>
      {x}/{y}/{z}
   </Colored>
)

export default EventPos
