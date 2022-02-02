import { DateTime } from 'luxon'
import { VFC } from 'react'
import { useParams } from 'react-router-dom'
import styled from 'styled-components'
import LinkSelectionBar from '../../components/LinkSelectionBar'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import BasePlayerList from '../../components/PlayerList'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'
import LoadingPage from '../LoadingPage'

const GameView: VFC = () => {
   const params = useParams()
   const { data } = useApi<Game<string>>(`game/${params.game}`)

   if (!data) return <LoadingPage />

   return (
      <Page>
         <Title>
            {data.name ?? data._id} - {DateTime.fromISO(data.startedAt).toLocaleString()}
         </Title>
         <PlayerList perRow={3}>
            {data.players.map(player => (
               <PlayerHead size='100px' key={player.uuid} {...player} />
            ))}
         </PlayerList>
         <LinkSelectionBar values={['events', 'stats']} />
      </Page>
   )
}

const PlayerList = styled(BasePlayerList)`
   width: fit-content;
   margin: 0 auto;
   margin-bottom: 2em;
`

export default GameView
