import { VFC } from 'react'
import { GameEvent } from '../types'

const GameEventInfo: VFC<GameEvent> = ({ action }) => (
   <>
      {action} game
   </>
)

export default GameEventInfo
