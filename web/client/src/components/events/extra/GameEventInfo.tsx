import { VFC } from 'react'
import { GameEvent } from '../../../models/events'

const GameEventInfo: VFC<GameEvent> = ({ action }) => (
   <>
      {action} game
   </>
)

export default GameEventInfo
