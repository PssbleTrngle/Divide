import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import { ScoreEvent } from '../types'

const ScoreEventInfo: VFC<ScoreEvent> = ({ player, objective, score }) => {
   return (
      <>
         <EventPlayer {...player} /> scored <Colored>{objective}</Colored> (now at {score})
      </>
   )
}

export default ScoreEventInfo
