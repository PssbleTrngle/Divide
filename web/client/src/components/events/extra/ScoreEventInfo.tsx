import { VFC } from 'react'
import { ScoreEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'

const ScoreEventInfo: VFC<ScoreEvent> = ({ player, objective, score }) => {
   return (
      <>
         <EventPlayer {...player} /> scored <Colored>{objective}</Colored> (now at {score})
      </>
   )
}

export default ScoreEventInfo
