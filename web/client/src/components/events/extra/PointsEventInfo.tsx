import { VFC } from 'react'
import { PointsEvent } from '../../../models/events'
import { Colored } from '../../Text'
import EventTeam from '../EventTeam'

const PointsEventInfo: VFC<PointsEvent> = ({ before, now, team, type }) => {
   const diff = Math.abs(now - before)
   const verb = now > before ? 'gained' : 'lost'
   return type === 'current' ? (
      <>
         <EventTeam {...team} /> {verb} <Colored>{diff}</Colored> points
      </>
   ) : (
      <>
         <EventTeam {...team} />
         {"'"}s total points are now at <Colored>{now}</Colored>
      </>
   )
}

export default PointsEventInfo
