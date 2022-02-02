import { VFC } from 'react'
import { useParams } from 'react-router-dom'
import Page from '../../components/Page'
import PlayerHead from '../../components/PlayerHead'
import { Code, Title } from '../../components/Text'
import useApi from '../../hooks/useApi'
import useLoading from '../../hooks/useLoading'
import { Player } from '../../models/player'

const PlayerView: VFC = () => {
   const { uuid } = useParams()
   const query = useApi<Player>(`player/${uuid}`)

   return useLoading(query, player => (
      <Page>
         <Title>{player.name}</Title>
         <PlayerHead {...player} />
         <Code>
            <pre>{JSON.stringify(player, null, 2)}</pre>
         </Code>
      </Page>
   ))
}

export default PlayerView
