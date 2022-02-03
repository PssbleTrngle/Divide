import { VFC } from 'react'
import styled from 'styled-components'
import EventChart from '../../components/charts/EventChart'
import EventPlayer from '../../components/events/EventPlayer'
import EventTeam from '../../components/events/EventTeam'
import BountyEventInfo from '../../components/events/extra/BountyEventInfo'
import RewardBoughtInfo from '../../components/events/extra/RewardBoughtInfo'
import Page from '../../components/Page'
import { Inline, Subtitle, Title } from '../../components/Text'
import { EventType, EventTypes } from '../../models/events'
import { Player, Team } from '../../models/player'
import { colorOf } from '../../util'

function getPlayer<T extends EventType>(supplier: (e: EventTypes[T]) => Player) {
   return (e: EventTypes[T]): Player & { id: string } => {
      const player = supplier(e)
      return { id: player.uuid, ...player }
   }
}

function getTeam<T extends EventType>(supplier: (e: EventTypes[T]) => Team) {
   return (e: EventTypes[T]): Omit<Team, 'color'> & { color?: string } => {
      const team = supplier(e)
      return { ...team, color: colorOf(team.color) }
   }
}

const Stats: VFC = () => {
   return (
      <Page>
         <Title>Stats</Title>

         <Layout>
            <Stat>
               <Subtitle>Points</Subtitle>
               <EventChart
                  type='points'
                  unit='points'
                  value={e => e.now}
                  group={e => e.type}
                  ownerOf={getTeam<'points'>(e => e.team)}
                  label={t => <EventTeam onlyHead={false} {...t} />}
               />
            </Stat>

            <Stat>
               <Subtitle>Scores</Subtitle>
               <EventChart
                  type='score'
                  value={e => e.score}
                  group={e => e.objective}
                  ownerOf={getPlayer<'score'>(e => e.player)}
                  label={(p, c) => <EventPlayer onlyHead={false} {...p} color={c} />}
               />
            </Stat>

            <Stat>
               <Subtitle>Rewards</Subtitle>
               <EventChart
                  type='reward'
                  unit='rewards bought'
                  value={(_, i) => i}
                  ownerOf={getTeam<'reward'>(e => e.boughtBy.team)}
                  label={t => <EventTeam onlyHead={false} {...t} />}
                  info={e => (
                     <Inline center>
                        <RewardBoughtInfo {...e} />
                     </Inline>
                  )}
               />
            </Stat>

            <Stat>
               <Subtitle>Bounties</Subtitle>
               <EventChart
                  type='bounty'
                  unit='fulfilled'
                  value={e => e.doneAlready}
                  ownerOf={getTeam<'bounty'>(e => e.fulfilledBy.team)}
                  label={t => <EventTeam onlyHead={false} {...t} />}
                  info={e => (
                     <Inline center>
                        <BountyEventInfo {...e} />
                     </Inline>
                  )}
               />
            </Stat>
         </Layout>
      </Page>
   )
}

const Stat = styled.div`
   display: grid;
   align-items: end;
`

const Layout = styled.section`
   display: grid;
   grid-template-columns: repeat(2, 1fr);
   column-gap: 2em;
   row-gap: 4em;

   @media (max-width: 1800px) {
      grid-template-columns: repeat(1, 1fr);
   }
`

export default Stats
