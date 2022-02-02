import { VFC } from 'react'
import EventChart from '../../components/charts/EventChart'
import EventPlayer from '../../components/events/EventPlayer'
import EventTeam from '../../components/events/EventTeam'
import Page from '../../components/Page'
import { Subtitle, Title } from '../../components/Text'
import { PointsEvent, ScoreEvent } from '../../models/events'
import { Player, Team } from '../../models/player'

function getDataPlayer({ player }: ScoreEvent): Player & { color: string; id: string } {
   return { id: player.uuid, ...player }
}

function getTeam(e: PointsEvent): Team & { color: string } {
   return e.team
}

const Stats: VFC = () => {
   return (
      <Page>
         <Title>Stats</Title>

         <Subtitle>Points</Subtitle>
         <EventChart
            type='points'
            value={e => e.now}
            group={e => e.type}
            ownerOf={getTeam}
            label={t => <EventTeam {...t} />}
         />

         <Subtitle>Scores</Subtitle>
         <EventChart
            type='score'
            unit=''
            value={e => e.score}
            group={e => e.objective}
            ownerOf={getDataPlayer}
            label={(p, c) => <EventPlayer {...p} color={c} />}
         />
      </Page>
   )
}

export default Stats
