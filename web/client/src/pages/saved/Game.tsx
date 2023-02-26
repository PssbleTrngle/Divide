import { DateTime } from 'luxon'
import { VFC } from 'react'
import { useParams } from 'react-router-dom'
import styled from 'styled-components'
import Link from '../../components/Link'
import LinkSelectionBar from '../../components/LinkSelectionBar'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import BasePlayerList from '../../components/PlayerList'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'
import { colorOf } from '../../util'
import LoadingPage from '../LoadingPage'

const GameView: VFC = () => {
   const params = useParams()
   const { data } = useApi<Game<string>>(`game/${params.game}`)

   if (!data) return <LoadingPage />
   const date = DateTime.fromISO(data.startedAt).toLocaleString()

   return (
      <Page>
         <Title>
            {data.name ?? data._id} - {date}
         </Title>
         <PlayerList center size='60px'>
            {data.players.map(player => (
               <Link key={player.uuid} to={`player/${player.uuid}`}>
                  <PlayerHead
                     highlight={params.uuid === player.uuid && colorOf(player.team.color)}
                     size='60px'
                     {...player}
                  />
               </Link>
            ))}
         </PlayerList>
         <LinkSelectionBar values={['events', 'stats']} />
      </Page>
   )
}

const PlayerList = styled(BasePlayerList)`
   margin-bottom: 2em;
`

export default GameView
