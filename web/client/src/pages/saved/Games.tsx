import { DateTime } from 'luxon'
import { VFC } from 'react'
import { Link } from 'react-router-dom'
import Page from '../../components/Page'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'

const Games: VFC = () => {
   const { data } = useApi<Game<string>[]>('game')

   return (
      <Page>
         <Title>Saved Games</Title>
         <ul>
            {data?.map(game => (
               <Link key={game._id} to={`/game/${game._id}`}>
                  {DateTime.fromISO(game.startedAt).toLocaleString()}
               </Link>
            ))}
         </ul>
      </Page>
   )
}

export default Games
