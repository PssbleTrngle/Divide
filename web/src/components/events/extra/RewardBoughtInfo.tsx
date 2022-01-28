import { VFC } from 'react'
import { Colored } from '../../Text'
import EventPlayer from '../EventPlayer'
import EventTeam from '../EventTeam'
import { RewardEvent } from '../types'

const RewardBoughtInfo: VFC<RewardEvent> = ({ reward, boughtBy, pointsPaid, target }) => (
   <>
      <EventPlayer {...boughtBy} /> used <Colored>{pointsPaid}</Colored> points to buy <Colored>{reward}</Colored>{' '}
      {target && (
         <>
            targeting <Target {...target} />
         </>
      )}
   </>
)

const Target: VFC<RewardEvent['target']> = target => {
   if (!target) return null
   if ('uuid' in target) return <EventPlayer {...target} />
   return <EventTeam {...target} />
}

export default RewardBoughtInfo
