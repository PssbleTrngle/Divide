import { VFC } from 'react'
import EventChart from '../components/charts/EventChart'
import Page from '../components/Page'
import { Subtitle, Title } from '../components/Text'

const Stats: VFC = () => {
   return (
      <Page>
         <Title>Stats</Title>

         <Subtitle>Points</Subtitle>
         <EventChart type='points' value={e => e.now} group={e => e.type} teamOf={e => e.team} />

         <Subtitle>Points</Subtitle>
         <EventChart
            type='score'
            unit=''
            value={e => e.score}
            group={e => e.objective}
            teamOf={e => ({ id: e.player.uuid, name: e.player.name, color: e.player.team?.color })}
         />
      </Page>
   )
}

export default Stats
