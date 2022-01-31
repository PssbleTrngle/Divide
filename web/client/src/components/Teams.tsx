import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { Player, Team } from '../models/player'
import Box from './Box'
import PlayerHead from './PlayerHead'
import TeamName from './TeamName'
import { Subtitle } from './Text'

const Teams: VFC = () => {
   const { data: teams } = useApi<Team[]>('team')
   const { data: players } = useApi<Player[]>('player')

   return (
      <Style>
         <Subtitle>Teams</Subtitle>
         <List>
            {teams?.map(({ name, id, color }) => (
               <section key={id}>
                  <Name>
                     <TeamName color={color}>{name}</TeamName>
                  </Name>
                  <Row>
                     {players
                        ?.filter(p => p.team?.id === id)
                        .map(player => (
                           <PlayerHead key={player.uuid} {...player} />
                        ))}
                  </Row>
               </section>
            ))}
         </List>
      </Style>
   )
}

const Name = styled.p`
   margin-bottom: 1em;
`

const List = styled.section`
   display: grid;
   gap: 2em;
   min-width: 500px;
`

const Row = styled.section`
   display: grid;
   grid-auto-flow: column;
   justify-content: center;
   gap: 0.5em;
`

const Style = styled(Box)`
   grid-area: teams;
`

export default Teams
