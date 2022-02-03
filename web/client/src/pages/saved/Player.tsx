import { VFC } from 'react'
import { useParams } from 'react-router-dom'
import styled from 'styled-components'
import Box from '../../components/Box'
import EventTeam from '../../components/events/EventTeam'
import GameLink from '../../components/GameLink'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import { Subtitle, Title } from '../../components/Text'
import useGameData from '../../hooks/useGameData'
import useLoading from '../../hooks/useLoading'
import { Player } from '../../models/player'

const PlayerView: VFC = () => {
   const { uuid } = useParams()
   const query = useGameData<Player>(`player/${uuid}`)

   return useLoading(query, player => (
      <Page>
         <Title>{player.name}</Title>
         <Style>
            <PlayerHead {...player} />
            {player.team && (
               <Team>
                  <EventTeam {...player.team} />
               </Team>
            )}
            <More>
               {player.scores && (
                  <>
                     <Subtitle>Scores</Subtitle>
                     {player.scores?.map(({ score, objective }) => (
                        <p key={objective}>
                           {objective}: {score}
                        </p>
                     ))}
                  </>
               )}
               {player.games && (
                  <>
                     <Subtitle>Games</Subtitle>
                     {player.games?.map(game => (
                        <GameLink key={game._id} {...game} />
                     ))}
                  </>
               )}
            </More>
         </Style>
      </Page>
   ))
}

const Style = styled(Box)`
   min-width: 300px;
   grid-template:
      'head team'
      'more more';
`

const More = styled(Box)`
   grid-area: more;
`

const Team = styled(Box)`
   grid-area: team;
`

export default PlayerView
