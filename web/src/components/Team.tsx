import { VFC } from 'react'
import styled from 'styled-components'
import useApi from '../hooks/useApi'
import { Player } from '../hooks/useSession'
import { colorOf } from '../util'
import Box from './Box'
import ErrorField from './ErrorField'
import PlayerHead from './PlayerHead'
import PlayerList from './PlayerList'
import { Subtitle } from './Text'

const Team: VFC = () => {
   const { data, error } = useApi<Player[]>('player?teammate=true')

   return (
      <Style>
         <Subtitle>Your Team</Subtitle>
         <ErrorField error={error} />
         <PlayerList perRow={2}>
            {data?.map(p => (
               <PlayerHead highlight={colorOf(p.team?.color)} key={p.uuid} {...p} />
            ))}
         </PlayerList>
      </Style>
   )
}

const Style = styled(Box)`
   grid-area: team;
`

export default Team
