import { VFC } from 'react'
import { useParams } from 'react-router-dom'
import LinkSelectionBar from '../../components/LinkSelectionBar'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import PlayerList from '../../components/PlayerList'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'

const GameView: VFC = () => {
   const params = useParams()
   const { data } = useApi<Game>(`game/${params.game}`)

   return (
      <Page>
         <Title>{data?._id}</Title>
         <PlayerList perRow={5}>
            {data?.players.map(player => (
               <PlayerHead size='100px' key={player.uuid} {...player} />
            ))}
         </PlayerList>
         <LinkSelectionBar values={['events', 'stats']} />
      </Page>
   )
}

export default GameView
