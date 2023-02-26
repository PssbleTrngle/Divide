import { VFC } from 'react'
import Box from '../../components/Box'
import GameLink from '../../components/GameLink'
import Head from '../../components/Head'
import Page from '../../components/Page'
import { Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import { Game } from '../../models/game'

const Games: VFC = () => {
   const { data } = useApi<Game<string>[]>('game')
   return (
      <Page>
         <Head title='Saved Games' />
         <Title>Saved Games</Title>
         <Box>
            {data?.map(game => (
               <GameLink key={game._id} {...game} />
            ))}
         </Box>
      </Page>
   )
}

export default Games
