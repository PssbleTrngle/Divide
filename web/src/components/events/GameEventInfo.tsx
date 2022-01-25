import { VFC } from 'react'
import { GameEvent } from './types'

const GameEventInfo: VFC<GameEvent> = ({ action }) => (
   <span>
      {action} game
   </span>
)

export default GameEventInfo
