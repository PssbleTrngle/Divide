import { VFC } from 'react'
import { Link } from 'react-router-dom'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import PlayerList from '../../components/PlayerList'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'
import { Player } from '../../models/player'

const Players: VFC = () => {
   const { data } = useApi<Array<Player & { games: Game[] }>>('player')

   return (
      <Page>
         <Title>Players</Title>
         <PlayerList size='100px'>
            {data?.map(player => (
               <Link key={player.uuid} to={player.uuid}>
                  <PlayerHead key={player.uuid} {...player} />
               </Link>
            ))}
         </PlayerList>
      </Page>
   )
}

export default Players
