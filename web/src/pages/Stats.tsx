import { VFC } from 'react'
import EventChart from '../components/charts/EventChart'
import Page from '../components/Page'
import { Title } from '../components/Text'

const Stats: VFC = () => {
   return (
      <Page>
         <Title>Stats</Title>
         <EventChart type='points' value={e => e.now} group={e => e.type} teamOf={e => e.team} />
      </Page>
   )
}

export default Stats
